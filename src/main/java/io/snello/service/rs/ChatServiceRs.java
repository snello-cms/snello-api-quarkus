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

import java.util.Map;

import static io.snello.management.AppConstants.CHAT_INTERACTIONS;
import static io.snello.management.AppConstants.CHAT_PATH;
import static io.snello.management.AppConstants.UUID;

@Path(CHAT_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatServiceRs {

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

        String reply = assistant.chat(conversationId, message);
        persistChatInteraction(conversationId, message, reply);
        return Response.ok(Map.of(
                "response", reply,
                "conversationId", conversationId
        )).build();
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
