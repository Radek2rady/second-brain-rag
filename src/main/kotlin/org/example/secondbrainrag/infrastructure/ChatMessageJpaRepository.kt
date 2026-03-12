package org.example.secondbrainrag.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageJpaRepository : JpaRepository<ChatMessageJpaEntity, String> {
    fun findByConversationIdAndTenantIdOrderByCreatedAtAsc(convId: String, tenantId: String): List<ChatMessageJpaEntity>

    @Query("SELECT DISTINCT c.conversationId FROM ChatMessageJpaEntity c WHERE c.tenantId = :tenantId ORDER BY MAX(c.createdAt) DESC")
    fun findConversationsByTenant(tenantId: String): List<String>

    @Query("SELECT c.conversationId FROM ChatMessageJpaEntity c GROUP BY c.conversationId ORDER BY MAX(c.createdAt) DESC")
    fun findConversationsOrderedByMostRecent(): List<String>
}
