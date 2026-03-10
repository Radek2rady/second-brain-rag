package org.example.secondbrainrag.infrastructure.web

import org.example.secondbrainrag.application.AuditService
import org.example.secondbrainrag.infrastructure.security.TokenProvider
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val tokenProvider: TokenProvider,
    private val auditService: AuditService
) {

    data class LoginRequest(val username: String)

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): Map<String, Any> {
        val roles = when (request.username.lowercase()) {
            "alice", "bob" -> listOf("ROLE_ADMIN", "ROLE_LEGAL_USER")
            "jan", "katka" -> listOf("ROLE_LEGAL_USER")
            else -> emptyList()
        }
        val token = tokenProvider.createToken(request.username, roles)
        
        auditService.logAction(
            username = request.username,
            tenantId = request.username, // using username as tenantId
            action = "LOGIN",
            details = "User ${request.username} logged in successfully",
            status = "SUCCESS"
        )
        
        return mapOf(
            "token" to token,
            "roles" to roles
        )
    }
}
