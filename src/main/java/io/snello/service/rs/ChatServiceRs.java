package io.snello.service.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.snello.model.ChatInteraction;
import io.snello.model.SearchResult;
import io.snello.service.ApiService;
import io.snello.service.ai.SnelloAssistant;
import io.snello.service.ai.AiRequestContext;
import io.snello.service.ai.tools.AiPaginationContextStore;
import io.snello.service.ai.tools.SnelloCmsTools;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.snello.management.AppConstants.CHAT_INTERACTIONS;
import static io.snello.management.AppConstants.CHAT_PATH;
import static io.snello.management.AppConstants.UUID;

@Path(CHAT_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatServiceRs {

    private static final Pattern ACTION_TOKEN_PATTERN = Pattern.compile("\\[ACTION:([^\\]]+)]");
    private static final Pattern LOAD_MORE_INTENT_PATTERN = Pattern.compile(
            "(?i)^\\s*(carica\\s+altri\\s+dati|carica\\s+altri|altri\\s+dati|continua|avanti|next(\\s+page)?|load\\s+more(\\s+data)?)\\s*[.!?]*\\s*$");
            private static final Set<String> SUMMARY_FIELDS = Set.of(
                "brand", "category", "length", "weight", "width", "level", "website_url");
            private static final Set<String> HIDDEN_FIELDS = Set.of(
                "uuid", "description", "principal_image", "creation_date", "lastupdate_date", "last_update_date");

    @Inject
    SnelloAssistant assistant;

    @Inject
    JsonWebToken jwt;

    @Inject
    ApiService apiService;

    @Inject
    AiRequestContext aiRequestContext;

    @Inject
    AiPaginationContextStore aiPaginationContextStore;

    @Inject
    SnelloCmsTools snelloCmsTools;

    @Inject
    ObjectMapper objectMapper;

    /**
     * POST /api/chat
     * Body: { "message": "..." }
     * Returns: { "response": "..." }
     */
    @POST
    public Response chat(Map<String, Object> body) throws Exception {
        if (body == null || body.get("message") == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Field 'message' is required"))
                    .build();
        }

        String message = body.get("message").toString();
        String conversationId = extractConversationId(body);
        boolean includeRawResponse = extractIncludeRawResponse(body);

        aiRequestContext.setConversationId(conversationId);

        if (isLoadMoreIntent(message)) {
            Response deterministicLoadMore = tryHandleLoadMore(message, conversationId, includeRawResponse);
            if (deterministicLoadMore != null) {
                return deterministicLoadMore;
            }
        }

        String reply = assistant.chat(conversationId, message);
        List<Map<String, Object>> actions = extractActions(reply);
        String cleanedReply = stripActionTokens(reply);
        persistChatInteraction(conversationId, message, cleanedReply);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("response", cleanedReply);
        responseBody.put("conversationId", conversationId);
        responseBody.put("actions", actions);
        if (includeRawResponse) {
            responseBody.put("rawResponse", reply);
        }
        return Response.ok(responseBody).build();
    }

    private Response tryHandleLoadMore(String userMessage, String conversationId, boolean includeRawResponse)
            throws Exception {
        AiPaginationContextStore.LastSearchContext context = aiPaginationContextStore.get(conversationId);
        if (context == null) {
            context = restorePaginationContextFromDb(conversationId);
            if (context == null) {
                return null;
            }
            aiPaginationContextStore.save(conversationId, context);
        }

        String toolReply = snelloCmsTools.loadMoreRecords();
        LoadMoreResponse loadMoreResponse = buildLoadMoreResponse(context, toolReply);
        String responseText = loadMoreResponse.responseText;
        persistChatInteraction(conversationId, userMessage, responseText);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("response", responseText);
        responseBody.put("conversationId", conversationId);
        responseBody.put("actions", loadMoreResponse.actions);
        if (includeRawResponse) {
            responseBody.put("rawResponse", toolReply);
        }
        return Response.ok(responseBody).build();
    }

    @SuppressWarnings("unchecked")
    private AiPaginationContextStore.LastSearchContext restorePaginationContextFromDb(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return null;
        }
        try {
            MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
            params.putSingle("conversation_uuid", conversationId);
            params.putSingle("user_message", AiPaginationContextStore.PAGINATION_CONTEXT_MARKER);
            params.putSingle("_sort", "creation_date:desc");

            List<Map<String, Object>> rows = apiService.list(CHAT_INTERACTIONS, params, null, 1, 0);
            if (rows == null || rows.isEmpty()) {
                return null;
            }

            Object rawJson = rows.get(0).get("ai_response");
            if (!(rawJson instanceof String json) || json.isBlank()) {
                return null;
            }

            Map<String, Object> payload = objectMapper.readValue(json, Map.class);
            String entityName = asString(payload.get("entityName"));
            Map<String, String> payloadParams = payload.get("params") instanceof Map<?, ?> map
                    ? (Map<String, String>) map
                    : Map.of();

            int limit = asInt(payload.get("limit"), 10);
            int nextStart = asInt(payload.get("nextStart"), 0);
            boolean hasMore = asBoolean(payload.get("hasMore"), true);

            return AiPaginationContextStore.LastSearchContext.of(entityName, payloadParams, limit, nextStart, hasMore);
        } catch (Exception e) {
            return null;
        }
    }

    private int asInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private boolean asBoolean(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = value.toString().trim();
        if (text.equalsIgnoreCase("true")) {
            return true;
        }
        if (text.equalsIgnoreCase("false")) {
            return false;
        }
        return defaultValue;
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private LoadMoreResponse buildLoadMoreResponse(AiPaginationContextStore.LastSearchContext context, String toolReply) {
        if (toolReply == null || toolReply.isBlank()) {
            return new LoadMoreResponse("Nessun dato restituito dal caricamento successivo.", List.of());
        }
        if (!toolReply.startsWith("{")) {
            return new LoadMoreResponse(toolReply, List.of());
        }
        try {
            SearchResult result = objectMapper.readValue(toolReply, SearchResult.class);
            if (result.records == null || result.records.isEmpty()) {
                return new LoadMoreResponse("Non ci sono altri dati da mostrare.", List.of());
            }

            List<Map<String, Object>> actions = buildOpenActions(context, result.records);
            return new LoadMoreResponse(formatSearchResult(context, result), actions);
        } catch (Exception e) {
            return new LoadMoreResponse(toolReply, List.of());
        }
    }

    private String formatSearchResult(AiPaginationContextStore.LastSearchContext context, SearchResult result) {
        String entity = context.entityName == null || context.entityName.isBlank() ? "risultati" : context.entityName;
        int from = result.records.isEmpty() ? result.start : result.start + 1;
        int to = result.start + result.records.size();

        StringBuilder sb = new StringBuilder();
        sb.append("Ecco altri risultati per ").append(entity).append(" (da ").append(from).append(" a ")
                .append(to).append("):\n\n");

        for (int index = 0; index < result.records.size(); index++) {
            Map<String, Object> record = result.records.get(index);
            appendRecordSummary(sb, record, result.start + index + 1);
            sb.append("\n");
        }

        if (result.hasMore) {
            sb.append("Ci sono altri ").append(result.remaining)
                    .append(" risultati disponibili. Se vuoi vedere più dati, puoi chiedere di \"Carica altri dati\".");
        } else {
            sb.append("Questi sono gli ultimi risultati disponibili.");
        }

        return sb.toString().trim();
    }

    private void appendRecordSummary(StringBuilder sb, Map<String, Object> record, int ordinal) {
        String title = firstNonBlank(record, "name", "title", "label", "uuid");
        String subtitle = firstNonBlank(record, "brand", "category");

        sb.append(ordinal).append(". **").append(title == null ? "Record" : title).append("**");
        if (subtitle != null && !subtitle.equals(title)) {
            sb.append(" - ").append(toDisplayValue(subtitle));
        }
        sb.append("\n");

        SUMMARY_FIELDS.stream()
                .filter(record::containsKey)
                .filter(field -> record.get(field) != null && !record.get(field).toString().isBlank())
                .sorted(Comparator.comparingInt(this::fieldPriority))
                .forEach(field -> sb.append("   - ").append(toDisplayLabel(field)).append(": ")
                        .append(toDisplayValue(record.get(field))).append("\n"));

        record.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().toString().isBlank())
                .filter(entry -> !SUMMARY_FIELDS.contains(entry.getKey()))
                .filter(entry -> !HIDDEN_FIELDS.contains(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .limit(3)
                .forEach(entry -> sb.append("   - ").append(toDisplayLabel(entry.getKey())).append(": ")
                        .append(toDisplayValue(entry.getValue())).append("\n"));

        Object website = record.get("website_url");
        if (website != null && !website.toString().isBlank()) {
            sb.append("   - [Dettagli](").append(website).append(")\n");
        }
    }

    private List<Map<String, Object>> buildOpenActions(AiPaginationContextStore.LastSearchContext context,
            List<Map<String, Object>> records) {
        String entity = context.entityName == null ? "" : context.entityName;
        List<Map<String, Object>> actions = new ArrayList<>();
        for (Map<String, Object> record : records) {
            Object uuid = record.get("uuid");
            if (uuid == null || uuid.toString().isBlank()) {
                continue;
            }
            Map<String, Object> action = new HashMap<>();
            action.put("type", "open");
            action.put("entity", entity);
            action.put("id", uuid.toString());
            actions.add(action);
        }
        return actions;
    }

    private String firstNonBlank(Map<String, Object> record, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Object value = record.get(fieldName);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }
        return null;
    }

    private int fieldPriority(String field) {
        return switch (field) {
            case "brand" -> 0;
            case "length" -> 1;
            case "weight" -> 2;
            case "width" -> 3;
            case "category" -> 4;
            case "level" -> 5;
            case "website_url" -> 6;
            default -> 10;
        };
    }

    private String toDisplayLabel(String field) {
        return switch (field) {
            case "website_url" -> "Dettagli";
            case "uuid" -> "ID";
            default -> Character.toUpperCase(field.charAt(0)) + field.substring(1).replace('_', ' ');
        };
    }

    private String toDisplayValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private static class LoadMoreResponse {
        private final String responseText;
        private final List<Map<String, Object>> actions;

        private LoadMoreResponse(String responseText, List<Map<String, Object>> actions) {
            this.responseText = responseText;
            this.actions = actions;
        }
    }

    private boolean isLoadMoreIntent(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        return LOAD_MORE_INTENT_PATTERN.matcher(message).matches();
    }

    private String stripActionTokens(String reply) {
        if (reply == null || reply.isBlank()) {
            return "";
        }
        String cleaned = ACTION_TOKEN_PATTERN.matcher(reply).replaceAll("");
        return cleaned.replaceAll("\\n{3,}", "\\n\\n").trim();
    }

    private boolean extractIncludeRawResponse(Map<String, Object> body) {
        Object value = body.get("includeRawResponse");
        return value instanceof Boolean && (Boolean) value;
    }

    private List<Map<String, Object>> extractActions(String reply) {
        List<Map<String, Object>> actions = new ArrayList<>();
        if (reply == null || reply.isBlank()) {
            return actions;
        }

        Matcher matcher = ACTION_TOKEN_PATTERN.matcher(reply);
        while (matcher.find()) {
            String actionBody = matcher.group(1);
            String[] parts = actionBody.split(":", 4);
            if (parts.length < 2) {
                continue;
            }

            String type = parts[0].trim().toLowerCase();
            Map<String, Object> action = new HashMap<>();
            action.put("type", type);

            switch (type) {
                case "open":
                    if (parts.length >= 3) {
                        action.put("entity", parts[1]);
                        action.put("id", parts[2]);
                        actions.add(action);
                    }
                    break;
                case "navigate":
                    action.put("path", actionBody.substring("NAVIGATE:".length()));
                    actions.add(action);
                    break;
                case "create_preview":
                    if (parts.length >= 3) {
                        action.put("entity", parts[1]);
                        action.put("payload", parts[2]);
                        actions.add(action);
                    }
                    break;
                default:
            }
        }

        return actions;
    }

    private void persistChatInteraction(String conversationId, String userMessage, String aiResponse) throws Exception {
        ChatInteraction interaction = new ChatInteraction();
        interaction.uuid = java.util.UUID.randomUUID().toString();
        interaction.conversation_uuid = conversationId;
        interaction.user_id = extractSessionUser();
        interaction.user_message = userMessage;
        interaction.ai_response = aiResponse;
        interaction.creation_date = java.time.LocalDateTime.now();
        apiService.create(CHAT_INTERACTIONS, interaction.toMap(), UUID);
    }

    private String extractConversationId(Map<String, Object> body) {
        Object conversationIdFromBody = body.get("conversationId");
        if (conversationIdFromBody != null && !conversationIdFromBody.toString().isBlank()) {
            return conversationIdFromBody.toString().trim();
        }

        if (jwt != null && jwt.getName() != null && !jwt.getName().isBlank()) {
            return "user:" + jwt.getName();
        }

        return "anonymous-default";
    }

    private String extractSessionUser() {
        if (jwt == null || jwt.getName() == null || jwt.getName().isBlank()) {
            return null;
        }
        return jwt.getName().trim();
    }
}
