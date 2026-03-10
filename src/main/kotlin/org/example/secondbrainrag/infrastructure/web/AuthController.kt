package org.example.secondbrainrag.infrastructure.web

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
    private val authenticationManager: AuthenticationManager
) {

    data class LoginRequest(val username: String, val password: String?)

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): Map<String, Any> {
        val auth: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password ?: "")
        )

        // Extract roles from the authenticated user
        val roles = auth.authorities.map { it.authority }

        val token = tokenProvider.createToken(request.username, roles)
        return mapOf(
            "token" to token,
            "roles" to roles
        )
    }
}
