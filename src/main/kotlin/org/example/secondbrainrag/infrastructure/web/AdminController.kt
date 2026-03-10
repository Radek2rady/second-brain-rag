package org.example.secondbrainrag.infrastructure.web

import org.example.secondbrainrag.domain.VectorDocumentPort
import org.example.secondbrainrag.infrastructure.audit.AuditEventRepository
import org.example.secondbrainrag.infrastructure.web.dto.AdminStatsDto
import org.example.secondbrainrag.infrastructure.web.dto.UserActivityDto
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val auditEventRepository: AuditEventRepository,
    private val vectorDocumentPort: VectorDocumentPort
) {

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    fun getStats(): AdminStatsDto {
        val yesterday = Instant.now().minus(24, ChronoUnit.HOURS)
        
        val totalDocs = vectorDocumentPort.countAll()
        val events24h = auditEventRepository.countSince(yesterday)
        
        val topUsersRaw = auditEventRepository.findTopUsers(PageRequest.of(0, 5))
        val topUsers = topUsersRaw.map {
            UserActivityDto(
                username = it["username"] as String,
                count = it["count"] as Long
            )
        }

        return AdminStatsDto(
            totalDocuments = totalDocs,
            auditEvents24h = events24h,
            topUsers = topUsers
        )
    }
}
