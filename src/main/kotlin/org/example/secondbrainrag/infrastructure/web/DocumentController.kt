package org.example.secondbrainrag.infrastructure.web

import org.example.secondbrainrag.application.AuditService
import org.example.secondbrainrag.application.DocumentService
import org.example.secondbrainrag.application.FileIngestionService
import org.example.secondbrainrag.application.IngestionService
import org.example.secondbrainrag.application.IngestionProgressTracker
import org.example.secondbrainrag.domain.ChatHistoryPort
import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.security.Principal
import org.springframework.security.core.Authentication
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/documents")
class DocumentController(
    private val documentService: DocumentService,
    private val ingestionService: IngestionService,
    private val fileIngestionService: FileIngestionService,
    private val chatHistoryPort: ChatHistoryPort,
    private val vectorDocumentPort: VectorDocumentPort,
    private val progressTracker: IngestionProgressTracker,
    private val auditService: AuditService
) {
    private val logger = LoggerFactory.getLogger(DocumentController::class.java)

    data class SaveRequest(val content: String, val metadata: Map<String, String>? = null)
    data class IngestRequest(val text: String, val metadata: Map<String, String>? = null)

    @PostMapping
    fun saveDocument(@RequestBody request: SaveRequest, principal: Principal): Map<String, String> {
        val doc = VectorDocument(
            content = request.content,
            metadata = request.metadata ?: emptyMap()
        )
        vectorDocumentPort.save(listOf(doc), principal.name)
        return mapOf("status" to "success", "id" to doc.id)
    }

    @PostMapping("/ingest")
    fun ingestDocument(@RequestBody request: IngestRequest, principal: Principal): Map<String, String> {
        ingestionService.ingest(request.text, request.metadata ?: emptyMap(), principal.name)
        return mapOf("status" to "success", "message" to "Text was chunked and ingested successfully")
    }

    @GetMapping("/search")
    fun search(
        @RequestParam query: String, 
        @RequestParam(defaultValue = "15") topK: Int, 
        principal: Principal
    ): List<VectorDocument> {
        auditService.logAction(principal.name, principal.name, "SEARCH", "Query: $query, topK: $topK")
        return documentService.searchSimilar(query, topK, principal.name)
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
        @RequestParam(required = false) conversationId: String?,
        principal: Principal
    ): ChatResponse {
        val result = documentService.chat(query, conversationId, principal.name)
        return ChatResponse(
            answer = result.answer,
            conversationId = result.conversationId,
            source = result.source.name,
            references = result.references
        )
    }

    @GetMapping("/conversations")
    fun getConversations(principal: Principal): List<String> {
        return chatHistoryPort.getConversations(principal.name)
    }

    @GetMapping("/chat/history")
    fun getChatHistory(@RequestParam conversationId: String, principal: Principal): List<Map<String, String>> {
        return chatHistoryPort.getLastMessages(conversationId, principal.name, 50).map {
            mapOf("role" to it.role, "content" to it.content)
        }
    }

    @GetMapping
    fun getAllDocuments(principal: Principal): List<VectorDocument> {
        return vectorDocumentPort.getAllDocuments(principal.name)
    }

    @DeleteMapping("/{id}")
    fun deleteDocument(@PathVariable id: String, principal: Principal): Map<String, String> {
        auditService.logAction(principal.name, principal.name, "DELETE", "Document ID: $id")
        vectorDocumentPort.deleteDocument(id, principal.name)
        return mapOf("status" to "success", "message" to "Document $id deleted")
    }

    @PostMapping("/upload")
    fun uploadFile(
        @RequestParam("file") file: MultipartFile, 
        @RequestParam(defaultValue = "PRIVATE") accessLevel: String,
        principal: Principal,
        authentication: Authentication
    ): ResponseEntity<Map<String, String>> {
        val isAdmin = authentication.authorities.any { it.authority == "ROLE_ADMIN" || it.authority == "ADMIN" }
        var finalAccessLevel = accessLevel.uppercase()
        
        if (finalAccessLevel == "GLOBAL" && !isAdmin) {
            logger.warn("User {} attempted to upload GLOBAL document without ADMIN role. Defaulting to COMPANY.", principal.name)
            finalAccessLevel = "COMPANY"
        }
        if (finalAccessLevel !in listOf("PRIVATE", "COMPANY", "GLOBAL")) {
            finalAccessLevel = "PRIVATE"
        }

        auditService.logAction(principal.name, principal.name, "UPLOAD", "File: ${file.originalFilename}, Access: $finalAccessLevel")
        return try {
            val jobId = fileIngestionService.ingestFile(file, principal.name, finalAccessLevel)
            ResponseEntity.accepted().body(mapOf(
                "status" to "processing",
                "jobId" to jobId,
                "message" to "File '${file.originalFilename}' is being processed in the background. Track progress via /api/documents/upload/status/$jobId"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "status" to "error",
                "message" to (e.message ?: "Invalid file")
            ))
        }
    }

    @GetMapping("/upload/status/{jobId}")
    fun getUploadStatus(@PathVariable jobId: String): ResponseEntity<Any> {
        val status = progressTracker.getStatus(jobId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(mapOf(
            "jobId" to status.jobId,
            "fileName" to status.fileName,
            "status" to status.status,
            "totalChunks" to status.totalChunks,
            "processedChunks" to status.processedChunks,
            "progressPercent" to status.progressPercent,
            "errorMessage" to (status.errorMessage ?: "")
        ))
    }

    @GetMapping("/upload/status")
    fun getAllUploadStatuses(): List<IngestionProgressTracker.IngestionStatus> {
        return progressTracker.getAllJobs()
    }
}

