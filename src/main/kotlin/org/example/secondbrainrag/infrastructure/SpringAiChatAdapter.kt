package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.ChatPort
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Component

@Component
class SpringAiChatAdapter(
    builder: ChatClient.Builder
) : ChatPort {

    private val chatClient: ChatClient = builder.build()

    override fun generateResponse(query: String, context: String): String {
        val systemPrompt = """
            Jsi expert na odpovídání z dokumentů. Odpovídej POUZE na základě poskytnutého kontextu. Pokud v kontextu odpověď není, řekni: 'Omlouvám se, ale o tomto v mém mozku nemám žádné informace.'
            KONTEXT: {context}
            DOTAZ: {query}
        """.trimIndent()

        return chatClient.prompt()
            .system { s ->
                s.text(systemPrompt)
                 .param("context", context)
                 .param("query", query)
            }
            .user(query)
            .call()
            .content() ?: "Omlouvám se, došlo k chybě při generování odpovědi."
    }
}
