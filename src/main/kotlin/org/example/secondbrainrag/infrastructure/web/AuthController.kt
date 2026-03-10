package org.example.secondbrainrag.infrastructure.web

import org.example.secondbrainrag.infrastructure.security.TokenProvider
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val tokenProvider: TokenProvider) {

    data class LoginRequest(val username: String)

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): Map<String, String> {
        // A simple test endpoint: give it any username and it returns a valid JWT token
        val token = tokenProvider.createToken(request.username)
        return mapOf("token" to token)
    }
}
