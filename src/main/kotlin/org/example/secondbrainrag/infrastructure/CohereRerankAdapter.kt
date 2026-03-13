package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.RerankPort
import org.example.secondbrainrag.domain.RerankedDocument
import org.example.secondbrainrag.domain.VectorDocument
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class CohereRerankAdapter(
    @Value("\${cohere.api-key}") private val apiKey: String
) : RerankPort {

    private val logger = LoggerFactory.getLogger(CohereRerankAdapter::class.java)
    private val restTemplate = RestTemplate()
    private val apiUrl = "https://api.cohere.ai/v1/rerank"

    data class RerankRequest(
        val model: String = "rerank-multilingual-v3.0",
        val query: String,
        val documents: List<String>,
        val top_n: Int
    )

    data class RerankResponse(
        val results: List<RerankResult>
    )

    data class RerankResult(
        val index: Int,
        val relevance_score: Double
    )

    override fun rerank(query: String, docs: List<VectorDocument>): List<RerankedDocument> {
        if (docs.isEmpty()) return emptyList()

        logger.info("Reranking {} documents for query: '{}'", docs.size, query)

        val request = RerankRequest(
            query = query,
            documents = docs.map { it.content },
            top_n = docs.size
        )

        val headers = org.springframework.http.HttpHeaders().apply {
            set("Authorization", "Bearer $apiKey")
            set("Content-Type", "application/json")
        }

        val entity = org.springframework.http.HttpEntity(request, headers)

        return try {
            val response = restTemplate.postForObject(apiUrl, entity, RerankResponse::class.java)
            
            val results = response?.results?.map { result ->
                RerankedDocument(
                    document = docs[result.index],
                    score = result.relevance_score
                )
            } ?: emptyList()

            logger.info("Cohere Rerank - Results count: {}, Top score: {}", 
                results.size, results.firstOrNull()?.score ?: 0.0)
            
            results
        } catch (e: Exception) {
            logger.error("Cohere rerank failed: {}", e.message)
            // Fallback: return original documents with 0.0 score or just empty list?
            // Requirement says "Return only the top 5 documents that have a rerank score higher than 0.1"
            // If it fails, we might want to return empty to avoid non-reranked results passing the threshold.
            emptyList()
        }
    }
}
