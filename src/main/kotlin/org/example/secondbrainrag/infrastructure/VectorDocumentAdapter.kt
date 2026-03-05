package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Component

@Component
class VectorDocumentAdapter(
    private val vectorStore: VectorStore
) : VectorDocumentPort {

    override fun save(documents: List<VectorDocument>) {
        saveAll(documents) // Delegating to saveAll for logic reuse
    }

    override fun saveAll(documents: List<VectorDocument>) {
        val springAiDocuments = documents.map { doc ->
            // Ensuring the ID is formatted as a valid UUID string
            val formattedId = java.util.UUID.fromString(doc.id).toString()
            Document(formattedId, doc.content, doc.metadata)
        }
        vectorStore.add(springAiDocuments)
    }

    override fun searchSimilar(query: String, topK: Int): List<VectorDocument> {
        val searchRequest = SearchRequest.builder().query(query).topK(topK).build()
        val results = vectorStore.similaritySearch(searchRequest)
        
        return results?.map { doc ->
            VectorDocument(
                id = java.util.UUID.fromString(doc.id).toString(),
                content = doc.text ?: "",
                metadata = doc.metadata.mapValues { it.value.toString() }
            )
        } ?: emptyList()
    }
}
