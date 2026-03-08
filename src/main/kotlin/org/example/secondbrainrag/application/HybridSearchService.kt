package org.example.secondbrainrag.application

import org.example.secondbrainrag.domain.FullTextSearchPort
import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class HybridSearchService(
    private val vectorDocumentPort: VectorDocumentPort,
    private val fullTextSearchPort: FullTextSearchPort
) {

    private val logger = LoggerFactory.getLogger(HybridSearchService::class.java)

    // Companion object no longer needed for regex as it's handled in the adapter

    /**
     * Performs hybrid search combining vector similarity and full-text search.
     * Always runs both search methods simultaneously and deduplicates their results.
     */
    fun search(query: String, topK: Int = 5): List<VectorDocument> {
        logger.info("Performing hybrid search for query='{}'", query)
        val ftResults = fullTextSearchPort.searchByKeyword(query, topK)
        val vectorResults = vectorDocumentPort.searchSimilar(query, topK)

        logger.info("Hybrid search: {} full-text results, {} vector results", ftResults.size, vectorResults.size)

        // Merge: full-text first, then vector (deduplicated by id)
        val seenIds = mutableSetOf<String>()
        val merged = mutableListOf<VectorDocument>()

        // Full-text results first (higher keyword precision)
        for (doc in ftResults) {
            if (seenIds.add(doc.id)) {
                merged.add(doc)
            }
        }

        // Then vector results (semantic similarity)
        for (doc in vectorResults) {
            if (seenIds.add(doc.id)) {
                merged.add(doc)
            }
        }

        logger.info("Hybrid merged total: {} unique results", merged.size)
        return merged
    }
}
