package org.example.secondbrainrag.domain

import java.util.UUID

/**
 * Domain entity representing a text document with metadata to be stored and embedded.
 */
data class VectorDocument(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val metadata: Map<String, Any> = emptyMap()
)
