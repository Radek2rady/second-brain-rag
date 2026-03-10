package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@Component
class VectorDocumentAdapter(
    private val vectorStore: VectorStore,
    private val jdbcTemplate: JdbcTemplate
) : VectorDocumentPort {

    private val logger = LoggerFactory.getLogger(VectorDocumentAdapter::class.java)
    private val objectMapper = jacksonObjectMapper()

    override fun save(documents: List<VectorDocument>, tenantId: String) {
        saveAll(documents, tenantId) // Delegating to saveAll for logic reuse
    }

    override fun saveAll(documents: List<VectorDocument>, tenantId: String) {
        val springAiDocuments = documents.map { doc ->
            // Ensuring the ID is formatted as a valid UUID string
            val formattedId = java.util.UUID.fromString(doc.id).toString()
            val metadataWithTenant = doc.metadata.toMutableMap()
            metadataWithTenant["tenantId"] = tenantId
            Document(formattedId, doc.content, metadataWithTenant)
        }
        vectorStore.add(springAiDocuments)
    }

    override fun searchSimilar(query: String, topK: Int, tenantId: String): List<VectorDocument> {
        val searchRequest = SearchRequest.builder()
            .query(query)
            .topK(topK)
            .similarityThreshold(0.60)
            .filterExpression("tenantId == '${tenantId}'")
            .build()
        val results = vectorStore.similaritySearch(searchRequest)

        logger.info("searchSimilar query='{}' returned {} raw results (threshold=0.60)", query, results?.size ?: 0)
        results?.forEachIndexed { i, doc ->
            val score = doc.metadata?.get("distance") ?: "N/A"
            logger.info("  result[{}]: score/distance={}, content='{}'", i, score, doc.text?.take(250) ?: "")
        }

        return results?.map { doc ->
            VectorDocument(
                id = java.util.UUID.fromString(doc.id).toString(),
                content = doc.text ?: "",
                metadata = doc.metadata.mapValues { it.value.toString() }
            )
        } ?: emptyList()
    }

    override fun getAllDocuments(tenantId: String): List<VectorDocument> {
        val sql = "SELECT id, content, metadata FROM vector_store WHERE metadata->>'tenantId' = ?"
        return jdbcTemplate.query(sql, { rs, _ ->
            // In pgvector, the id is usually a UUID string, content is text, metadata is JSONB
            // We'll deserialize metadata manually if needed, but for the UI we might only need id and content or minimal metadata.
            val id = rs.getString("id") ?: ""
            val content = rs.getString("content") ?: ""
            
            // Fix: Deserialize metadata
            val metadataStr = rs.getString("metadata") ?: "{}"
            val parsedMetadata = try {
                objectMapper.readValue<Map<String, String>>(metadataStr)
            } catch (e: Exception) {
                logger.warn("Failed to parse metadata JSON in getAlLDuruments: {}", metadataStr)
                mapOf("raw" to metadataStr)
            }
            
            VectorDocument(
                id = id,
                content = content,
                metadata = parsedMetadata
            )
        }, tenantId)
    }

    override fun deleteDocument(id: String, tenantId: String) {
        val count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM vector_store WHERE id = ?::uuid AND metadata->>'tenantId' = ?", 
            Int::class.java, 
            id, 
            tenantId
        )
        if (count != null && count > 0) {
            vectorStore.delete(listOf(id))
            logger.info("Deleted document {} for tenant {}", id, tenantId)
        } else {
            logger.warn("Attempt to delete document {} by unauthorized tenant {}", id, tenantId)
        }
    }
}
