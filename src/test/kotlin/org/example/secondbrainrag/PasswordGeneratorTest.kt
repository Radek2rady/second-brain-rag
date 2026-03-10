package org.example.secondbrainrag

import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class PasswordGeneratorTest {
    @Test
    fun generatePasswordHash() {
        val encoder = BCryptPasswordEncoder()
        val hash = encoder.encode("password")
        println("==================================================")
        println("GENERATED HASH FOR 'password': $hash")
        println("==================================================")
    }
}
