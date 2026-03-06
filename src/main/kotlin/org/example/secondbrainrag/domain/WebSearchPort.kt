package org.example.secondbrainrag.domain

/**
 * Port (Interface) for web search capabilities.
 * Belongs to the Domain layer — adapters provide the actual implementation
 * (e.g. Tavily API, or a mock for testing).
 */
interface WebSearchPort {
    /**
     * Searches the web for the given query.
     *
     * @param query The search query
     * @param maxResults Maximum number of results to return
     * @return List of web search results, empty list on failure (graceful degradation)
     */
    fun search(query: String, maxResults: Int = 3): List<WebSearchResult>
}
