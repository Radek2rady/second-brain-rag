package org.example.secondbrainrag.domain

/**
 * Port (Interface) for saving and querying vector documents.
 * This lives in the Domain layer and has no dependency on Spring AI or the Database.
 */
interface VectorDocumentPort {
    
    /**
     * Saves a list of documents into the vector store.
     */
    fun save(documents: List<VectorDocument>, tenantId: String)

    /**
     * Saves a list of documents into the vector store in bulk format.
     */
    fun saveAll(documents: List<VectorDocument>, tenantId: String)

    /**
     * Searches for documents similar to the given query text.
     * 
     * @param query The text to search for
     * @param topK Number of top results to return
     * @param tenantId The ID of the tenant searching
     * @return List of matching documents
     */
    fun searchSimilar(query: String, topK: Int = 4, tenantId: String): List<VectorDocument>

    /**
     * Retrieves all documents stored in the database for the given tenant.
     */
    fun getAllDocuments(tenantId: String): List<VectorDocument>

    /**
     * Deletes a specific document by its ID.
     */
    fun deleteDocument(id: String, tenantId: String)

    /**
     * Counts all documents across all tenants.
     * This is intended for admin dashboard purposes.
     */
    fun countAll(): Long
}
