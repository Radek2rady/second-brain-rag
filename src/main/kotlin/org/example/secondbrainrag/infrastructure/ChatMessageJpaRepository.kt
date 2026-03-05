package org.example.secondbrainrag.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageJpaRepository : JpaRepository<ChatMessageJpaEntity, String> {
    fun findByConversationIdOrderByCreatedAtAsc(conversationId: String): List<ChatMessageJpaEntity>
}
