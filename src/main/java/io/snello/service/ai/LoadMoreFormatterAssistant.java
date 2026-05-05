package io.snello.service.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface LoadMoreFormatterAssistant {

    @SystemMessage("""
            You format paginated follow-up responses for a CMS assistant.
            
            Rules:
            - Always respond in the same language as the user's latest message.
            - If a previous assistant response is provided, imitate its visible style, structure, tone, and level of detail as closely as possible.
            - Never return raw JSON.
            - Never invent fields or facts that are not present in the provided result payload.
            - Keep markdown formatting when useful.
            - Do not include action tags. Those are handled elsewhere.
            - If the status says there are no more results or no previous context, answer naturally in the user's language.
            """)
    String format(@UserMessage String prompt);
}