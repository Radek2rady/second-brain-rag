package org.example.secondbrainrag.domain

/**
 * Port for reranking search results to improve precision.
 */
interface RerankPort {
    /**
     * Reranks the provided documents based on their relevance to the query.
     * Returns a list of documents with their relevance scores potentially updated (depending on implementation).
     */
    fun rerank(query: String, docs: List<VectorDocument>): List<RerankedDocument>
}

/**
 * Data class representing a document with its rerank score.
 */
data class RerankedDocument(
    val document: VectorDocument,
    val score: Double
)
