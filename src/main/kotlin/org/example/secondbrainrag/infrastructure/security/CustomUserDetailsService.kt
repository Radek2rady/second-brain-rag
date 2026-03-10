package org.example.secondbrainrag.infrastructure.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val userEntity = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        var cleanRole = userEntity.role.uppercase()
        println("DEBUG: userEntity.role = ${userEntity.role}")
        while (cleanRole.startsWith("ROLE_")) {
            cleanRole = cleanRole.removePrefix("ROLE_")
        }
        val finalRole = "ROLE_$cleanRole"
        println("DEBUG: final role assigned = $finalRole")
        val authorities = listOf(SimpleGrantedAuthority(finalRole))

        return User(
            userEntity.username,
            userEntity.hashedPassword,
            authorities
        )
    }
}
