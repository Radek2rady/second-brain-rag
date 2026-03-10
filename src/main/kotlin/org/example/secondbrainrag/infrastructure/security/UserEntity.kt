package org.example.secondbrainrag.infrastructure.security

import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(name = "hashed_password", nullable = false)
    val hashedPassword: String,

    @Column(nullable = false)
    val role: String,

    @Column(name = "department_id")
    val departmentId: String? = null
)
