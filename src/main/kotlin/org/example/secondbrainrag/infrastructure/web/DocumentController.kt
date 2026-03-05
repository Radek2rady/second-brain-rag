package org.example.secondbrainrag.infrastructure.web

import org.example.secondbrainrag.application.DocumentService
import org.example.secondbrainrag.domain.VectorDocument
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/documents")
class DocumentController(
    private val documentService: DocumentService
) {

    data class SaveRequest(val content: String, val metadata: Map<String, String>? = null)

    @PostMapping
    fun saveDocument(@RequestBody request: SaveRequest): Map<String, String> {
        val doc = VectorDocument(
            content = request.content,
            metadata = request.metadata ?: emptyMap()
        )
        documentService.saveDocuments(listOf(doc))
        return mapOf("status" to "success", "id" to doc.id)
    }

    @GetMapping("/search")
    fun search(@RequestParam query: String, @RequestParam(defaultValue = "4") topK: Int): List<VectorDocument> {
        return documentService.searchSimilar(query, topK)
    }
}
