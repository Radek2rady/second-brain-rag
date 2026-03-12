package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.ChatHistoryPort
import org.example.secondbrainrag.domain.ChatMessage
import org.springframework.stereotype.Component

@Component
class JpaChatHistoryAdapter(
    private val repository: ChatMessageJpaRepository
) : ChatHistoryPort {

    override fun saveMessage(conversationId: String, tenantId: String, message: ChatMessage) {
        repository.save(ChatMessageJpaEntity(
            conversationId = conversationId,
            tenantId = tenantId,
            role = message.role,
            content = message.content
        ))
    }

    override fun getLastMessages(conversationId: String, tenantId: String, limit: Int): List<ChatMessage> {
        val entities = repository.findByConversationIdAndTenantIdOrderByCreatedAtAsc(conversationId, tenantId)
        return entities.takeLast(limit).map { ChatMessage(it.role, it.content) }
    }

    override fun getConversations(tenantId: String): List<String> {
        return repository.findConversationsByTenant(tenantId)
    }
}
