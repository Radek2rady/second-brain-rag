package org.example.secondbrainrag.infrastructure.web

import org.example.secondbrainrag.infrastructure.audit.AuditEventJpaEntity
import org.example.secondbrainrag.infrastructure.audit.AuditEventRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/audit")
class AuditController(private val auditEventRepository: AuditEventRepository) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAuditLogs(
        @org.springframework.web.bind.annotation.RequestParam(defaultValue = "") username: String,
        @org.springframework.web.bind.annotation.RequestParam(defaultValue = "") action: String,
        @PageableDefault(size = 50, sort = ["timestamp"], direction = Sort.Direction.DESC) pageable: Pageable
    ): Page<AuditEventJpaEntity> {
        return auditEventRepository.findByUsernameContainingIgnoreCaseAndActionContainingIgnoreCase(
            username, 
            action, 
            pageable
        )
    }
}
