package org.example.secondbrainrag.application

import org.example.secondbrainrag.domain.ChatPort
import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort
import org.springframework.stereotype.Service

@Service
class DocumentService(
    private val vectorDocumentPort: VectorDocumentPort,
    private val chatPort: ChatPort
) {

    fun saveDocuments(documents: List<VectorDocument>) {
        vectorDocumentPort.save(documents)
    }

    fun searchSimilar(query: String, topK: Int = 4): List<VectorDocument> {
        return vectorDocumentPort.searchSimilar(query, topK)
    }

    fun chat(query: String): String {
        // 1. Retrieve relevant documents
        val similarDocuments = searchSimilar(query)
        
        // 2. Combine document contents into context
        val context = similarDocuments.joinToString(separator = "\n\n") { it.content }
        
        // 3. Delegate generation to ChatPort within the Hexagonal Architecture
        return chatPort.generateResponse(query, context)
    }
}
