package org.example.secondbrainrag.application

import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort
import org.springframework.stereotype.Service

@Service
class DocumentService(
    private val vectorDocumentPort: VectorDocumentPort
) {

    fun saveDocuments(documents: List<VectorDocument>) {
        vectorDocumentPort.save(documents)
    }

    fun searchSimilar(query: String, topK: Int = 4): List<VectorDocument> {
        return vectorDocumentPort.searchSimilar(query, topK)
    }
}
