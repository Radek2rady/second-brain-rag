package org.example.secondbrainrag.application

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileIngestionService(
    private val ingestionService: IngestionService
) {

    companion object {
        private const val MAX_FILE_SIZE = 5 * 1024 * 1024L // 5MB
        private val ALLOWED_EXTENSIONS = setOf("txt", "md")
    }

    fun ingestFile(file: MultipartFile) {
        // Validate file size
        if (file.size > MAX_FILE_SIZE) {
            throw IllegalArgumentException("Soubor je příliš velký. Maximální velikost je 5MB.")
        }

        // Validate file extension
        val extension = file.originalFilename
            ?.substringAfterLast('.', "")
            ?.lowercase()
            ?: throw IllegalArgumentException("Soubor nemá platnou příponu.")

        if (extension !in ALLOWED_EXTENSIONS) {
            throw IllegalArgumentException("Nepodporovaný typ souboru. Povolené typy: ${ALLOWED_EXTENSIONS.joinToString(", ")}.")
        }

        // Read content
        val content = file.inputStream.bufferedReader().readText()

        if (content.isBlank()) {
            throw IllegalArgumentException("Soubor je prázdný.")
        }

        // Delegate to existing IngestionService with filename metadata
        val metadata = mapOf(
            "source" to (file.originalFilename ?: "unknown"),
            "type" to extension
        )
        ingestionService.ingest(content, metadata)
    }
}
