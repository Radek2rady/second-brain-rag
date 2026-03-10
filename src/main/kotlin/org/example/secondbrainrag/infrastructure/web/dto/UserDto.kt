package org.example.secondbrainrag.infrastructure.web.dto

data class UserDto(
    val id: Long?,
    val username: String,
    val role: String,
    val departmentId: String?
)

data class RoleUpdateRequest(
    val role: String
)
