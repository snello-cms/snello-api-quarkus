package io.snello.service.ai.tools;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AiPaginationContextStore {

    private final ConcurrentHashMap<String, LastSearchContext> contexts = new ConcurrentHashMap<>();

    public void save(String conversationId, LastSearchContext context) {
        if (conversationId == null || conversationId.isBlank() || context == null) {
            return;
        }
        contexts.put(conversationId, context);
    }

    public LastSearchContext get(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return null;
        }
        return contexts.get(conversationId);
    }

    public static class LastSearchContext {
        public String entityName;
        public HashMap<String, String> params;
        public int limit;
        public int nextStart;
        public boolean hasMore;

        public static LastSearchContext of(String entityName, Map<String, String> params, int limit, int nextStart,
                boolean hasMore) {
            LastSearchContext context = new LastSearchContext();
            context.entityName = entityName;
            context.params = params == null ? new HashMap<>() : new HashMap<>(params);
            context.limit = limit;
            context.nextStart = nextStart;
            context.hasMore = hasMore;
            return context;
        }
    }
}