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

    override fun generateResponse(query: String, context: String, history: List<ChatMessage>): String {
        val systemPrompt = """
            Jsi expert na odpovídání z dokumentů. Odpovídej POUZE na základě poskytnutého kontextu. Pokud v kontextu odpověď není, řekni: 'Omlouvám se, ale o tomto v mém mozku nemám žádné informace.'
            KONTEXT: {context}
            DOTAZ: {query}
        """.trimIndent()

        val messages: List<Message> = history.map { 
            if (it.role == "user") UserMessage(it.content) else AssistantMessage(it.content)
        }

        return chatClient.prompt()
            .system { s ->
                s.text(systemPrompt)
                 .param("context", context)
                 .param("query", query)
            }
            .messages(messages)
            .user(query)
            .call()
            .content() ?: "Omlouvám se, došlo k chybě při generování odpovědi."
    }
}
