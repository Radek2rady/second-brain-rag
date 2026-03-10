package org.example.secondbrainrag.application

import org.example.secondbrainrag.domain.audit.AuditEvent
import org.example.secondbrainrag.domain.audit.AuditPort
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class AuditService(private val auditPort: AuditPort) {

    @Async
    fun logAction(username: String, tenantId: String, action: String, details: String?, status: String = "SUCCESS") {
        val event = AuditEvent(
            username = username,
            tenantId = tenantId,
            action = action,
            details = details,
            status = status
        )
        auditPort.logEvent(event)
    }
}
