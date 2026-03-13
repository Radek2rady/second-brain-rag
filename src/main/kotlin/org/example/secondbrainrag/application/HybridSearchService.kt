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
        
        // 2. Reranking Stage - ALWAYS use the ORIGINAL query for maximum precision
        logger.info("Reranking {} candidates via Cohere using ORIGINAL query: '{}'", totalCandidates.size, originalQuery)
        val finalResults = try {
            val rerankedResults = rerankPort.rerank(originalQuery, totalCandidates)

            // Pokud rerank vrátí výsledky, filtrujeme je
            rerankedResults
                .sortedByDescending { it.score }
                .filter { it.score > 0.05 }
                .take(5)
                .map { it.document }
        } catch (e: Exception) {
            // KLÍČOVÝ FIX: Pokud Cohere vybuchne (401), nezahazuj data!
            logger.error("Reranking failed ({}). Using raw candidates as fallback.", e.message)
            totalCandidates.take(5)
        }

        logger.info("Search completed: {} documents proceeding to AI", finalResults.size)
        return finalResults
    }
}
