package io.snello.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.HashMap;
import java.util.Map;

@RegisterForReflection
public class ChatInteraction {

    public String uuid;
    public String conversation_uuid;
    public String session_user;
    public String user_message;
    public String ai_response;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (uuid != null) {
            map.put("uuid", uuid);
        }
        if (conversation_uuid != null) {
            map.put("conversation_uuid", conversation_uuid);
        }
        if (session_user != null) {
            map.put("session_user", session_user);
        }
        if (user_message != null) {
            map.put("user_message", user_message);
        }
        if (ai_response != null) {
            map.put("ai_response", ai_response);
        }
        return map;
    }
}
