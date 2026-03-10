package org.example.secondbrainrag.domain.audit

import java.time.Instant
import java.util.UUID

data class AuditEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Instant = Instant.now(),
    val username: String,
    val tenantId: String,
    val action: String,
    val details: String?,
    val status: String // SUCCESS, FAILED, DENIED
)
