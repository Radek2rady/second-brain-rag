package org.example.secondbrainrag.infrastructure.web

import org.example.secondbrainrag.application.DocumentService
import org.example.secondbrainrag.application.IngestionService
import org.example.secondbrainrag.domain.VectorDocument
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/documents")
class DocumentController(
    private val documentService: DocumentService,
    private val ingestionService: IngestionService
) {

    data class SaveRequest(val content: String, val metadata: Map<String, String>? = null)
    data class IngestRequest(val text: String, val metadata: Map<String, String>? = null)

    @PostMapping
    fun saveDocument(@RequestBody request: SaveRequest): Map<String, String> {
        val doc = VectorDocument(
            content = request.content,
            metadata = request.metadata ?: emptyMap()
        )
        documentService.saveDocuments(listOf(doc))
        return mapOf("status" to "success", "id" to doc.id)
    }

    @PostMapping("/ingest")
    fun ingestDocument(@RequestBody request: IngestRequest): Map<String, String> {
        ingestionService.ingest(request.text, request.metadata ?: emptyMap())
        return mapOf("status" to "success", "message" to "Text was chunked and ingested successfully")
    }

    @GetMapping("/search")
    fun search(@RequestParam query: String, @RequestParam(defaultValue = "4") topK: Int): List<VectorDocument> {
        return documentService.searchSimilar(query, topK)
    }

    data class ChatResponse(val answer: String, val conversationId: String)

    @GetMapping("/chat")
    fun chat(
        @RequestParam query: String,
        @RequestParam(required = false) conversationId: String?
    ): ChatResponse {
        val (activeConversationId, answer) = documentService.chat(query, conversationId)
        return ChatResponse(answer, activeConversationId)
    }
}
