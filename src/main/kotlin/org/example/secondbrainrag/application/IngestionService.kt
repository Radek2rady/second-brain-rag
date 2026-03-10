package org.example.secondbrainrag.application

import org.example.secondbrainrag.domain.DocumentSplitterPort
import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class IngestionService(
    private val splitterPort: DocumentSplitterPort,
    private val vectorDocumentPort: VectorDocumentPort
) {

    private val logger = LoggerFactory.getLogger(IngestionService::class.java)

    companion object {
        /** Batch size for embedding + saving to avoid OpenAI API timeouts */
        private const val BATCH_SIZE = 20
    }

    /**
     * Ingests a raw large text, splits it into chunks, and saves them to the vector store.
     * Uses batch processing to avoid overwhelming the embedding API.
     *
     * @return total number of chunks saved
     */
    fun ingest(content: String, metadata: Map<String, String> = emptyMap(), tenantId: String): Int {
        logger.info("Starting ingestion for tenant {}: content length={} chars, metadata={}", tenantId, content.length, metadata)

        val chunks = splitterPort.splitText(content)
        logger.info("Text split into {} chunks", chunks.size)

        val documents = chunks.map { chunkContent ->
            VectorDocument(
                content = chunkContent,
                metadata = metadata
            )
        }

        // Batch processing to avoid OpenAI API rate limits and timeouts
        val batches = documents.chunked(BATCH_SIZE)
        logger.info("Saving {} documents in {} batches (batch size={})", documents.size, batches.size, BATCH_SIZE)

        batches.forEachIndexed { index, batch ->
            logger.info("Saving batch {}/{} ({} documents) for tenant {}...", index + 1, batches.size, batch.size, tenantId)
            try {
                vectorDocumentPort.saveAll(batch, tenantId)
                logger.info("Batch {}/{} saved successfully", index + 1, batches.size)
            } catch (e: Exception) {
                logger.error("Failed to save batch {}/{}: {}", index + 1, batches.size, e.message, e)
                throw e
            }
        }

        logger.info("Ingestion complete: {} chunks saved successfully", documents.size)
        return documents.size
    }

    /**
     * Ingests with progress tracking for async processing.
     *
     * @return total number of chunks saved
     */
    fun ingestWithProgress(
        content: String,
        metadata: Map<String, String> = emptyMap(),
        tenantId: String,
        onProgress: (totalChunks: Int, processedChunks: Int) -> Unit
    ): Int {
        logger.info("Starting ingestion with progress for tenant {}: content length={} chars", tenantId, content.length)

        val chunks = splitterPort.splitText(content)
        logger.info("Text split into {} chunks", chunks.size)

        val documents = chunks.map { chunkContent ->
            VectorDocument(content = chunkContent, metadata = metadata)
        }

        val batches = documents.chunked(BATCH_SIZE)
        var processedCount = 0

        onProgress(documents.size, 0)

        batches.forEachIndexed { index, batch ->
            logger.info("Saving batch {}/{} ({} documents) for tenant {}...", index + 1, batches.size, batch.size, tenantId)
            try {
                vectorDocumentPort.saveAll(batch, tenantId)
                processedCount += batch.size
                onProgress(documents.size, processedCount)
                logger.info("Batch {}/{} saved. Progress: {}/{}", index + 1, batches.size, processedCount, documents.size)
            } catch (e: Exception) {
                logger.error("Failed at batch {}/{}: {}", index + 1, batches.size, e.message, e)
                throw e
            }
        }

        logger.info("Ingestion complete: {} chunks saved successfully", documents.size)
        return documents.size
    }
}
