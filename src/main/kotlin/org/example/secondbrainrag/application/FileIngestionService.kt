package org.example.secondbrainrag.application

import org.example.secondbrainrag.infrastructure.PdfParsingAdapter
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.slf4j.LoggerFactory
import java.util.UUID

@Service
class FileIngestionService(
    private val ingestionService: IngestionService,
    private val pdfParsingAdapter: PdfParsingAdapter,
    private val progressTracker: IngestionProgressTracker
) {

    private val logger = LoggerFactory.getLogger(FileIngestionService::class.java)

    companion object {
        private const val MAX_FILE_SIZE = 50 * 1024 * 1024L // 50MB
        private val ALLOWED_EXTENSIONS = setOf("txt", "md", "pdf")
    }

    /**
     * Starts file ingestion asynchronously and returns a job ID for progress tracking.
     */
    fun ingestFile(file: MultipartFile, tenantId: String): String {
        // Validate file size
        if (file.size > MAX_FILE_SIZE) {
            throw IllegalArgumentException("File is too large. Maximum size is 50MB.")
        }

        // Validate file extension
        val extension = file.originalFilename
            ?.substringAfterLast('.', "")
            ?.lowercase()
            ?: throw IllegalArgumentException("File does not have a valid extension.")

        if (extension !in ALLOWED_EXTENSIONS) {
            throw IllegalArgumentException("Unsupported file type. Allowed types: ${ALLOWED_EXTENSIONS.joinToString(", ")}.")
        }

        val fileName = file.originalFilename ?: "unknown"
        logger.info("Starting file ingestion for '{}' (size={} bytes, type={})", fileName, file.size, extension)

        // Extract text synchronously (must happen while MultipartFile is still available)
        val content = when (extension) {
            "pdf" -> pdfParsingAdapter.extractText(file)
            else -> {
                val text = file.inputStream.bufferedReader().readText()
                if (text.isBlank()) {
                    throw IllegalArgumentException("File is empty.")
                }
                text
            }
        }

        logger.info("Extracted {} characters from '{}', starting async ingestion...", content.length, fileName)

        // Create job ID and start async processing
        val jobId = UUID.randomUUID().toString()
        val metadata = mapOf(
            "fileName" to fileName,
            "documentType" to extension
        )

        progressTracker.startJob(jobId, fileName)
        processIngestionAsync(jobId, content, metadata, fileName, tenantId)

        return jobId
    }

    /**
     * Processes ingestion in a separate thread to avoid HTTP timeout.
     */
    @Async
    fun processIngestionAsync(jobId: String, content: String, metadata: Map<String, String>, fileName: String, tenantId: String) {
        try {
            logger.info("[Job {}] Async ingestion started for '{}' (tenant: {})", jobId, fileName, tenantId)

            val totalChunks = ingestionService.ingestWithProgress(content, metadata, tenantId) { total, processed ->
                progressTracker.updateProgress(jobId, total, processed)
                logger.info("[Job {}] Progress: {}/{} chunks ({}%)",
                    jobId, processed, total,
                    if (total > 0) (processed * 100) / total else 0
                )
            }

            progressTracker.completeJob(jobId, totalChunks)
            logger.info("[Job {}] Ingestion COMPLETED for '{}': {} chunks saved", jobId, fileName, totalChunks)

        } catch (e: Exception) {
            logger.error("[Job {}] Ingestion FAILED for '{}': {}", jobId, fileName, e.message, e)
            progressTracker.failJob(jobId, e.message ?: "Unknown error")
        }
    }
}
