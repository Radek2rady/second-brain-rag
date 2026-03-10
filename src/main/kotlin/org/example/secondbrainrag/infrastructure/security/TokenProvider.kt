package org.example.secondbrainrag.infrastructure.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class TokenProvider {
    // Generate a secure key for HS256
    private val key: SecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    private val validityInMilliseconds: Long = 3600000 * 24 // 24 hours

    fun createToken(username: String): String {
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key)
            .compact()
    }

    fun getUsername(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
            true
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
