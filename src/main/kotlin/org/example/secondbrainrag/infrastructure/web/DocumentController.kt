package org.example.secondbrainrag.infrastructure.web

import org.example.secondbrainrag.application.DocumentService
import org.example.secondbrainrag.application.FileIngestionService
import org.example.secondbrainrag.application.IngestionService
import org.example.secondbrainrag.domain.ChatHistoryPort
import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/documents")
class DocumentController(
    private val documentService: DocumentService,
    private val ingestionService: IngestionService,
    private val fileIngestionService: FileIngestionService,
    private val chatHistoryPort: ChatHistoryPort,
    private val vectorDocumentPort: VectorDocumentPort
) {

    data class SaveRequest(val content: String, val metadata: Map<String, String>? = null)
    data class IngestRequest(val text: String, val metadata: Map<String, String>? = null)

    @PostMapping
    fun saveDocument(@RequestBody request: SaveRequest): Map<String, String> {
        val doc = VectorDocument(
            content = request.content,
            metadata = request.metadata ?: emptyMap()
        )
        vectorDocumentPort.save(listOf(doc))
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

    data class ChatResponse(
        val answer: String,
        val conversationId: String,
        val source: String,
        val references: List<String> = emptyList()
    )

    @GetMapping("/chat")
    fun chat(
        @RequestParam query: String,
        @RequestParam(required = false) conversationId: String?
    ): ChatResponse {
        val result = documentService.chat(query, conversationId)
        return ChatResponse(
            answer = result.answer,
            conversationId = result.conversationId,
            source = result.source.name,
            references = result.references
        )
    }

    @GetMapping("/conversations")
    fun getConversations(): List<String> {
        return chatHistoryPort.getConversations()
    }

    @GetMapping("/chat/history")
    fun getChatHistory(@RequestParam conversationId: String): List<Map<String, String>> {
        return chatHistoryPort.getLastMessages(conversationId, 50).map {
            mapOf("role" to it.role, "content" to it.content)
        }
    }

    @GetMapping
    fun getAllDocuments(): List<VectorDocument> {
        return vectorDocumentPort.getAllDocuments()
    }

    @DeleteMapping("/{id}")
    fun deleteDocument(@PathVariable id: String): Map<String, String> {
        vectorDocumentPort.deleteDocument(id)
        return mapOf("status" to "success", "message" to "Document $id deleted")
    }

    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
        return try {
            fileIngestionService.ingestFile(file)
            ResponseEntity.ok(mapOf(
                "status" to "success",
                "message" to "File '${file.originalFilename}' uploaded and ingested successfully"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "status" to "error",
                "message" to (e.message ?: "Invalid file")
            ))
        }
    }
}
