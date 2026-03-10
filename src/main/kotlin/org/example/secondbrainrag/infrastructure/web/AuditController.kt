package org.example.secondbrainrag.infrastructure.web

import org.example.secondbrainrag.infrastructure.audit.AuditEventJpaEntity
import org.example.secondbrainrag.infrastructure.audit.AuditEventRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/audit")
class AuditController(private val auditEventRepository: AuditEventRepository) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAuditLogs(): List<AuditEventJpaEntity> {
        return auditEventRepository.findAll().sortedByDescending { it.timestamp }
    }
}
