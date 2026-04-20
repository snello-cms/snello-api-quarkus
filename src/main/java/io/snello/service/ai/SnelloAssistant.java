package io.snello.service.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(tools = SnelloCmsTools.class)
public interface SnelloAssistant {

    @SystemMessage("""
            You are the virtual assistant of snello.io CMS.
            Your role is to help users navigate, query and manage content in the CMS.
            Before any data entry operation, ALWAYS invoke the 'getEntitySchema' tool
            to discover the available fields and their types for the target entity.
            Never invent field names that are not present in the metadata schema.
            When listing or fetching records, use the available tools instead of guessing.
            Respond in the same language used by the user.
            """)
    String chat(@MemoryId Object conversationId, @UserMessage String message);
}
