package org.example.secondbrainrag.infrastructure.audit

import org.example.secondbrainrag.domain.audit.AuditEvent
import org.example.secondbrainrag.domain.audit.AuditPort
import org.springframework.stereotype.Component

@Component
class AuditAdapter(private val repository: AuditEventRepository) : AuditPort {
    override fun logEvent(event: AuditEvent) {
        val entity = AuditEventJpaEntity(
            id = event.id,
            timestamp = event.timestamp,
            username = event.username,
            tenantId = event.tenantId,
            action = event.action,
            details = event.details,
            status = event.status
        )
        repository.save(entity)
    }
}
