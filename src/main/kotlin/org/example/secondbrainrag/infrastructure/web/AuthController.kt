package org.example.secondbrainrag.infrastructure.web

import org.example.secondbrainrag.application.AuditService
import org.example.secondbrainrag.infrastructure.security.TokenProvider
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
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
    private val log = LoggerFactory.getLogger(javaClass)

    data class LoginRequest(val username: String, val password: String?)

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): Map<String, Any> {
        val encoder = org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
        println("DEBUG: Nový hash pro 'password': " + encoder.encode("password"))

        val auth: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password ?: "")
        )

        SecurityContextHolder.getContext().authentication = auth

        val roles = auth.authorities.map { it.authority }
        val token = tokenProvider.createToken(request.username, roles)

        auditService.logAction(
            username = request.username,
            tenantId = request.username,
            action = "LOGIN",
            details = "User ${request.username} logged in successfully",
            status = "SUCCESS"
        )

        log.info("User {} successfully authenticated", request.username)

        return mapOf(
            "token" to token,
            "roles" to roles
        )
    }
}