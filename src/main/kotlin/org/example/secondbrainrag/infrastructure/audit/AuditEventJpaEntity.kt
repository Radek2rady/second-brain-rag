package org.example.secondbrainrag.infrastructure.audit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "audit_events")
class AuditEventJpaEntity(
    @Id
    val id: String,
    
    val timestamp: Instant,
    
    val username: String,
    
    @Column(name = "tenant_id")
    val tenantId: String,
    
    val action: String,
    
    @Column(columnDefinition = "TEXT")
    val details: String?,
    
    val status: String
)
