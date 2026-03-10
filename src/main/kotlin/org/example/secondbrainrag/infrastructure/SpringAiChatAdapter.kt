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

            6. If the context does not contain an answer, DO NOT ATTEMPT to answer from your own knowledge. Tell the user honestly. The AI MUST NOT hallucinate.

            ## LARGE DOCUMENTS AND PRECISE SECTION MATCHES:
            - You may have very extensive legal and regulatory documents in the context (civil code, building code, etc.).
            - If the query contains a reference to a section (§), paragraph, article, or item, you MUST find and cite the EXACT wording of the given provision from the provided context.
            - Search the ENTIRE provided context from beginning to end before answering. Do not just look at the beginning.
            - If you find an exact section, cite it VERBATIM and give the exact source (file name).
            - Only if you DO NOT FIND an exact section match in the context, tell the user honestly.
            - NEVER provide a "general answer" or "summary" if the context contains the exact text of the section. Always cite exactly.
            - If there are multiple sections in the context, answer the one that was in the query.

            CONTEXT: {context}
            QUERY: {query}
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
