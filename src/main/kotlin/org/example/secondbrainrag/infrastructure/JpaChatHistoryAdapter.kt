package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.ChatHistoryPort
import org.example.secondbrainrag.domain.ChatMessage
import org.springframework.stereotype.Component

@Component
class JpaChatHistoryAdapter(
    private val repository: ChatMessageJpaRepository
) : ChatHistoryPort {

    override fun saveMessage(conversationId: String, message: ChatMessage) {
        val entity = ChatMessageJpaEntity(
            conversationId = conversationId,
            role = message.role,
            content = message.content
        )
        repository.save(entity)
    }

    override fun getLastMessages(conversationId: String, limit: Int): List<ChatMessage> {
        // Find all for the conversation ordered by creation time ascending
        val allMessages = repository.findByConversationIdOrderByCreatedAtAsc(conversationId)
        
        // Take the latest $limit messages (we reverse, take limit, and reverse back so they stay chronological)
        // Alternatively, since findBy returns ascending (oldest first), we can takeLast
        val recentEntities = allMessages.takeLast(limit)
        
        return recentEntities.map { entity ->
            ChatMessage(
                role = entity.role,
                content = entity.content
            )
        }
    }

    override fun getConversations(): List<String> {
        return repository.findConversationsOrderedByMostRecent()
    }
}
