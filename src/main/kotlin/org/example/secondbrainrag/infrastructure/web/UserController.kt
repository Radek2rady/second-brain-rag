package org.example.secondbrainrag.infrastructure.web

import org.example.secondbrainrag.infrastructure.security.UserRepository
import org.example.secondbrainrag.infrastructure.web.dto.RoleUpdateRequest
import org.example.secondbrainrag.infrastructure.web.dto.UserDto
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userRepository: UserRepository) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(): List<UserDto> {
        return userRepository.findAll().map {
            UserDto(
                id = it.id,
                username = it.username,
                role = it.role,
                departmentId = it.departmentId
            )
        }
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateUserRole(
        @PathVariable id: Long,
        @RequestBody request: RoleUpdateRequest
    ): ResponseEntity<UserDto> {
        val user = userRepository.findById(id).orElseThrow {
            IllegalArgumentException("User $id not found")
        }
        
        // Ensure role is formatted properly (e.g., without ROLE_ prefix if needed by DB, but standard is exactly what app expects)
        val newRole = request.role.removePrefix("ROLE_").uppercase()
        
        // create new entity since fields are val
        val updatedUser = org.example.secondbrainrag.infrastructure.security.UserEntity(
            id = user.id,
            username = user.username,
            hashedPassword = user.hashedPassword,
            role = newRole,
            departmentId = user.departmentId
        )
        
        val saved = userRepository.save(updatedUser)
        return ResponseEntity.ok(
            UserDto(
                id = saved.id,
                username = saved.username,
                role = saved.role,
                departmentId = saved.departmentId
            )
        )
    }
}
