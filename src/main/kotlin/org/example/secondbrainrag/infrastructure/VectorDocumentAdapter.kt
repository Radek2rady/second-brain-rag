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
        val springAiDocuments = documents.map { doc ->
            Document(doc.id, doc.content, doc.metadata)
        }
        vectorStore.add(springAiDocuments)
    }

    override fun searchSimilar(query: String, topK: Int): List<VectorDocument> {
        val searchRequest = SearchRequest.builder().query(query).topK(topK).build()
        val results = vectorStore.similaritySearch(searchRequest)
        
        return results?.map { doc ->
            VectorDocument(
                id = doc.id,
                content = doc.text ?: "",
                metadata = doc.metadata.mapValues { it.value.toString() }
            )
        } ?: emptyList()
    }
}
