package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.ChatMessage
import org.example.secondbrainrag.domain.ChatPort
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.messages.Message
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SpringAiChatAdapter(
    builder: ChatClient.Builder
) : ChatPort {

    private val logger = LoggerFactory.getLogger(SpringAiChatAdapter::class.java)

    private val chatClient: ChatClient = builder.build()

    override fun generateResponse(query: String, context: String, history: List<ChatMessage>, sourceHint: String): String {
        val systemPrompt = """
            You are "Second Brain" – a highly intelligent corporate assistant. 
            Your goal is to provide precise, professional, and authoritative answers.

            ## SOURCE MANAGEMENT:
            Current Source Mode: {sourceHint}

            1. LOCAL DOCUMENTS (Priority #1):
               - When answering from local context, ALWAYS start or include: "Podle dokumentu [filename]..."
               - If the context contains legal sections like § or paragraphs, cite them exactly (e.g., "§ 123 (Občanský zákoník)").
               - Be confident. Do NOT apologize for using local data.

            2. WEB SEARCH (Priority #2):
               - If information is not in local documents, use web sources.
               - In this case, start with: "⚠️ Následující informace pocházejí z internetu, nikoliv z vašich dokumentů:"
               - If you find contradictory info on the web, add: "Varování: Webové zdroje uvádějí rozporuplné informace."

            3. HYBRID MODE:
               - Clearly separate local and web info using headings: "📁 Z vašich dokumentů:" and "🌐 Z internetu:".

            ## BEHAVIORAL RULES:
            - LANGUAGE: ALWAYS respond in the same language the user used (Czech for Czech, English for English).
            - TONE: Professional, concise, and direct. No "fluff". Use bullet points.
            - NO CONTRADICTION: Never say "I found nothing in documents" if you are currently providing an answer from the web. Just provide the answer and label the source.
            - ACCURACY: If information is missing in both local context and web, admit it. Do not hallucinate.

            ## CONTEXT:
            {context}
        """.trimIndent()

        val messages: List<Message> = history.map {
            if (it.role == "user") UserMessage(it.content) else AssistantMessage(it.content)
        }

        logger.info("AI Request - Query: '{}', Context Size: {} chars", query, context.length)

        val response = chatClient.prompt()
            .system { s ->
                s.text(systemPrompt)
                    .param("context", context)
                    .param("sourceHint", sourceHint)
            }
            .messages(messages)
            .user(query)
            .call()
            .content() ?: "Omlouvám se, došlo k chybě při generování odpovědi."

        logger.info("AI Response - Content: '{}'", response.take(100) + if (response.length > 100) "..." else "")
        return response
    }
}