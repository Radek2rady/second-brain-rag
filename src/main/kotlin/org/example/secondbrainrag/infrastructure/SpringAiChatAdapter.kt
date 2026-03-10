package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.ChatMessage
import org.example.secondbrainrag.domain.ChatPort
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.stereotype.Component

@Component
class SpringAiChatAdapter(
    builder: ChatClient.Builder
) : ChatPort {

    private val chatClient: ChatClient = builder.build()

    override fun generateResponse(query: String, context: String, history: List<ChatMessage>, sourceHint: String): String {
        val systemPrompt = """
            You are a precise legal researcher and an expert in answering from documents. Your answers must be absolutely accurate and always supported by sources.

            ## MAIN RULES:

            1. LOCAL CONTEXT AND LOGICAL CONNECTION:
            - Use the provided context to answer the question. Even if the context does not use the same words (e.g., 'carrier' instead of 'postman'), logically connect these terms to the context.
            - If you find a relevant principle in the context (e.g., danger of damage), explain it proactively to the user.

            2. CONTEXT SOURCE: {sourceHint}

            3. If the source is "LOCAL":
               - Answer EXCLUSIVELY based on the provided context from the user's local documents.
               - ALWAYS state the name of the source document. In the context, it is marked as "--- SOURCE: [filename] ---". Cite it in the response in the format: **Source: [filename]**.
               - If the text contains section symbols (§), paragraphs, or articles, ALWAYS include them in the citation. Format: **§ [number] ([document name])**.
               - If the context contains multiple documents, state the source for each claim separately.
               - If the answer is NOT in the context, say: "I'm sorry, but I don't have any information about this in the uploaded documents." NEVER invent or supplement information that is not in the context.
               - NEVER answer from your own knowledge. Only and exclusively from the provided context.

            3. If the source is "WEB":
               - At the beginning of the reply, you MUST state: "⚠️ The following information comes from an internet search, NOT from your uploaded documents:"
               - Emphasize that web sources may be politicized, contain noise, be outdated, or inaccurate.
               - If you find conflicting information, you MUST warn: "Warning: I found conflicting information in web sources – verify the facts from trusted primary sources."
               - Never present web information as verified facts.

            4. If the source is "HYBRID":
               - Clearly separate information from local documents and from the web.
               - Use headings: "📁 From your documents:" and "🌐 From the internet:"
               - For local information, follow the rules from point 2 (document names, sections).
               - For web information, follow the rules from point 3 (warning).

            5. NEVER LIE about the origin of the information. If you are not sure, ADMIT IT. Say: "I'm not sure, this is just my guess."

            You are a highly capable AI Assistant for the "Second Brain RAG" system.
            Your goal is to answer user queries using the provided context from the user's personal documents.
            
            STRICT GUIDELINES:
            1. LANGUAGE: Respond ONLY in English. Do not use any other language even if the user asks.
            2. CITATIONS: When using information from the provided context, you MUST cite the source using brackets like [1], [2], etc.
            3. SOURCES: Always provide at least 1-2 citations if the context allows.
            4. UNCERTAINTY: If you cannot find the answer in the provided context, explicitly state that you are answering from your general knowledge, but still try to be helpful.
            5. FORMATTING: Use clear Markdown (bullet points, bold text) for readability.
            
            Context:
            {context}
        """.trimIndent()

        val messages: List<Message> = history.map { 
            if (it.role == "user") UserMessage(it.content) else AssistantMessage(it.content)
        }

        return chatClient.prompt()
            .system { s ->
                s.text(systemPrompt)
                 .param("context", context)
                 .param("query", query)
                 .param("sourceHint", sourceHint)
            }
            .messages(messages)
            .user(query)
            .call()
            .content() ?: "I'm sorry, an error occurred while generating the response."
    }
}
