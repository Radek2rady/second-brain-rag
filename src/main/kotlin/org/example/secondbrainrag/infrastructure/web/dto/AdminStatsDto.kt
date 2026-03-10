package org.example.secondbrainrag.infrastructure.web.dto

data class AdminStatsDto(
    val totalDocuments: Long,
    val auditEvents24h: Long,
    val topUsers: List<UserActivityDto>
)

data class UserActivityDto(
    val username: String,
    val count: Long
)
