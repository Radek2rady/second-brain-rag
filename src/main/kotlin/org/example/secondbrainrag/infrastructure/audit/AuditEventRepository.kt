package org.example.secondbrainrag.infrastructure.audit

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface AuditEventRepository : JpaRepository<AuditEventJpaEntity, String> {
    
    @Query("SELECT COUNT(e) FROM AuditEventJpaEntity e WHERE e.timestamp > :since")
    fun countSince(since: Instant): Long

    fun findByUsernameContainingIgnoreCaseAndActionContainingIgnoreCase(
        username: String,
        action: String,
        pageable: org.springframework.data.domain.Pageable
    ): org.springframework.data.domain.Page<AuditEventJpaEntity>

    @Query("""
        SELECT e.username as username, COUNT(e) as count 
        FROM AuditEventJpaEntity e 
        GROUP BY e.username 
        ORDER BY COUNT(e) DESC
    """)
    fun findTopUsers(pageable: org.springframework.data.domain.Pageable): List<Map<String, Any>>
}
