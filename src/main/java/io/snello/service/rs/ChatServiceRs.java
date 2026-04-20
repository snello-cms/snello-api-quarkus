package io.snello.service.rs;

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

import static io.snello.management.AppConstants.CHAT_PATH;

@Path(CHAT_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatServiceRs {

    @Inject
    SnelloAssistant assistant;

    @Inject
    JsonWebToken jwt;

    /**
     * POST /api/chat
     * Body: { "message": "..." }
     * Returns: { "response": "..." }
     */
    @POST
    public Response chat(Map<String, Object> body) {
        if (body == null || body.get("message") == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Field 'message' is required"))
                    .build();
        }

        String message = body.get("message").toString();
        String conversationId = extractConversationId(body);

        String reply = assistant.chat(conversationId, message);
        return Response.ok(Map.of(
                "response", reply,
                "conversationId", conversationId
        )).build();
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
}
