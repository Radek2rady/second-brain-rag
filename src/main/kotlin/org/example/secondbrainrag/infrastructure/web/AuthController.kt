package org.example.secondbrainrag.infrastructure.web

import org.example.secondbrainrag.application.AuditService
import org.example.secondbrainrag.infrastructure.security.TokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val tokenProvider: TokenProvider,
    private val authenticationManager: AuthenticationManager,
    private val auditService: AuditService
) {

    data class LoginRequest(val username: String, val password: String?)

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): Map<String, Any> {
        val auth: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password ?: "")
        )

        val roles = auth.authorities.map { it.authority }

        val token = tokenProvider.createToken(request.username, roles)
        
        auditService.logAction(
            username = request.username,
            tenantId = request.username,
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
