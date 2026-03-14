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
       - When answering from local context, ALWAYS start or include: "📁 Z vašich dokumentů:" and "Podle dokumentu [filename]...".
       - If the context contains legal sections like § or paragraphs, cite them exactly (e.g., "§ 123 (Občanský zákoník)").
       - **STRICT CITATION RULE:** Only cite a local document if the information is EXPLICITLY and factually present in that document. 
       - **FORBIDDEN:** Never attribute general knowledge (e.g., recipes, historical facts not in context) to a local file just because that file is present in the context.

    2. WEB SEARCH (Priority #2):
       - If information is not in local documents, use your general knowledge/web sources.
       - In this case, start with: "⚠️ Následující informace pocházejí z internetu, nikoliv z vašich dokumentů:".
       - **NEVER** cite a local filename in this section.

    3. HYBRID MODE:
       - Clearly separate local and web info using headings: "📁 Z vašich dokumentů:" and "🌐 Z internetu:".

    ## BEHAVIORAL RULES:
    - LANGUAGE: ALWAYS respond in the same language the user used (Czech for Czech, English for English).
    - TONE: Professional, concise, and direct. Use bullet points.
    - NO HALLUCINATION: If information is missing in both local context and web, admit it. 
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