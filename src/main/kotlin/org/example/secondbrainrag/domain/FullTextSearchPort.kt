package org.example.secondbrainrag.domain

/**
 * Port for full-text keyword search against stored documents.
 * Uses PostgreSQL tsvector/tsquery under the hood.
 */
interface FullTextSearchPort {
    /**
     * Searches documents by keyword/phrase using full-text search.
     *
     * @param query The raw search query (may contain §, numbers, Czech text)
     * @param topK Number of top results to return
     * @param tenantId The tenant ID to filter results by
     * @return List of matching documents ranked by relevance
     */
    fun searchByKeyword(query: String, topK: Int = 5, tenantId: String): List<VectorDocument>
}
