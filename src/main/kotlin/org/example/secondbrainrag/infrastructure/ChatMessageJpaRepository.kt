package org.example.secondbrainrag.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageJpaRepository : JpaRepository<ChatMessageJpaEntity, String> {
    fun findByConversationIdOrderByCreatedAtAsc(conversationId: String): List<ChatMessageJpaEntity>

    @Query("SELECT c.conversationId FROM ChatMessageJpaEntity c GROUP BY c.conversationId ORDER BY MAX(c.createdAt) DESC")
    fun findConversationsOrderedByMostRecent(): List<String>
}
