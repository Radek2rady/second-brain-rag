package org.example.secondbrainrag.domain.audit

interface AuditPort {
    fun logEvent(event: AuditEvent)
}
