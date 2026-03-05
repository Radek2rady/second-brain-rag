package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class VectorDocumentAdapter(
    private val vectorStore: VectorStore,
    private val jdbcTemplate: JdbcTemplate
) : VectorDocumentPort {

    override fun save(documents: List<VectorDocument>) {
        saveAll(documents) // Delegating to saveAll for logic reuse
    }

    override fun saveAll(documents: List<VectorDocument>) {
        val springAiDocuments = documents.map { doc ->
            // Ensuring the ID is formatted as a valid UUID string
            val formattedId = java.util.UUID.fromString(doc.id).toString()
            Document(formattedId, doc.content, doc.metadata)
        }
        vectorStore.add(springAiDocuments)
    }

    override fun searchSimilar(query: String, topK: Int): List<VectorDocument> {
        val searchRequest = SearchRequest.builder().query(query).topK(topK).build()
        val results = vectorStore.similaritySearch(searchRequest)
        
        return results?.map { doc ->
            VectorDocument(
                id = java.util.UUID.fromString(doc.id).toString(),
                content = doc.text ?: "",
                metadata = doc.metadata.mapValues { it.value.toString() }
            )
        } ?: emptyList()
    }

    override fun getAllDocuments(): List<VectorDocument> {
        val sql = "SELECT id, content, metadata FROM vector_store"
        return jdbcTemplate.query(sql) { rs, _ ->
            // In pgvector, the id is usually a UUID string, content is text, metadata is JSONB
            // We'll deserialize metadata manually if needed, but for the UI we might only need id and content or minimal metadata.
            val id = rs.getString("id") ?: ""
            val content = rs.getString("content") ?: ""
            // metadata fallback is to just store the raw JSON string in our Map
            val metadataStr = rs.getString("metadata") ?: "{}"
            
            VectorDocument(
                id = id,
                content = content,
                metadata = mapOf("raw" to metadataStr) // Simple wrapping
            )
        }
    }

    override fun deleteDocument(id: String) {
        vectorStore.delete(listOf(id))
    }
}
