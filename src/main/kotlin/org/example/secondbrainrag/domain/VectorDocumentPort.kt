package org.example.secondbrainrag.domain

/**
 * Port (Interface) for saving and querying vector documents.
 * This lives in the Domain layer and has no dependency on Spring AI or the Database.
 */
interface VectorDocumentPort {
    
    /**
     * Saves a list of documents into the vector store.
     */
    fun save(documents: List<VectorDocument>)

    /**
     * Searches for documents similar to the given query text.
     * 
     * @param query The text to search for
     * @param topK Number of top results to return
     * @return List of matching documents
     */
    fun searchSimilar(query: String, topK: Int = 4): List<VectorDocument>
}
