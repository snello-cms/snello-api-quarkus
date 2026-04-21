package io.snello.service.rs;

import io.snello.model.ChatInteraction;
import io.snello.service.ApiService;
import io.snello.service.ai.SnelloAssistant;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Inject
    SnelloAssistant assistant;

    @Inject
    JsonWebToken jwt;

    @Inject
    ApiService apiService;

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
