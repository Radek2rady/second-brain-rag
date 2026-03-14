package org.example.secondbrainrag.application

import org.example.secondbrainrag.domain.FullTextSearchPort
import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class HybridSearchService(
    private val vectorDocumentPort: VectorDocumentPort,
    private val fullTextSearchPort: FullTextSearchPort,
    private val rerankPort: org.example.secondbrainrag.domain.RerankPort
) {

    private val logger = LoggerFactory.getLogger(HybridSearchService::class.java)

    /**
     * Performs hybrid search combining vector similarity and full-text search,
     * followed by a reranking stage for high precision.
     */
    fun search(originalQuery: String, expandedQuery: String, topK: Int = 15, tenantId: String): List<VectorDocument> {
        logger.info("Performing hybrid search for expandedQuery='{}' (original='{}')", expandedQuery, originalQuery)

        // Capture more candidates for reranking (up to 20)
        val candidateLimit = 20
        val ftResults = fullTextSearchPort.searchByKeyword(expandedQuery, candidateLimit, tenantId)
        val vectorResults = vectorDocumentPort.searchSimilar(expandedQuery, candidateLimit, tenantId)

        logger.info("Hybrid search for tenant {}: {} full-text results, {} vector results", tenantId, ftResults.size, vectorResults.size)

        // Merge: full-text first, then vector (deduplicated by id)
        val seenIds = mutableSetOf<String>()
        val candidates = mutableListOf<VectorDocument>()

        for (doc in ftResults) {
            if (seenIds.add(doc.id)) {
                candidates.add(doc)
            }
        }

        for (doc in vectorResults) {
            if (seenIds.add(doc.id)) {
                candidates.add(doc)
            }
        }

        val totalCandidates = candidates.take(candidateLimit)
        
        // 2. Reranking Stage - AMPUTATED. Taking top 5-10 direct candidates immediately.
        // We completely skip the reranking logic (rerankPort) due to 401 Unauthorized errors from Cohere.
        // Take Top 8 candidates directly
        val finalResults = totalCandidates.take(8)

        // Clear logging as requested
        logger.info("Hybrid search returning {} direct candidates for AI context (Reranking skipped).", finalResults.size)
        return finalResults
    }
}
