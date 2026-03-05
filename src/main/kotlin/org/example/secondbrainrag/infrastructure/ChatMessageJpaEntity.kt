package org.example.secondbrainrag.infrastructure

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "chat_messages")
class ChatMessageJpaEntity(
    @Id
    val id: String = UUID.randomUUID().toString(),
    
    @Column(nullable = false)
    val conversationId: String,
    
    @Column(nullable = false)
    val role: String,
    
    @Column(columnDefinition = "TEXT", nullable = false)
    val content: String,
    
    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
) {
    // Required by JPA
    protected constructor() : this(
        conversationId = "",
        role = "",
        content = ""
    )
}
