package org.example.secondbrainrag.application

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory tracker for ingestion job progress.
 * Frontend can poll /api/documents/upload/status/{jobId} to check progress.
 */
@Component
class IngestionProgressTracker {

    data class IngestionStatus(
        val jobId: String,
        val fileName: String,
        val status: String, // PROCESSING, COMPLETED, FAILED
        val totalChunks: Int = 0,
        val processedChunks: Int = 0,
        val errorMessage: String? = null
    ) {
        val progressPercent: Int
            get() = if (totalChunks > 0) (processedChunks * 100) / totalChunks else 0
    }

    private val jobs = ConcurrentHashMap<String, IngestionStatus>()

    fun startJob(jobId: String, fileName: String) {
        jobs[jobId] = IngestionStatus(jobId = jobId, fileName = fileName, status = "PROCESSING")
    }

    fun updateProgress(jobId: String, totalChunks: Int, processedChunks: Int) {
        jobs.computeIfPresent(jobId) { _, current ->
            current.copy(totalChunks = totalChunks, processedChunks = processedChunks)
        }
    }

    fun completeJob(jobId: String, totalChunks: Int) {
        jobs.computeIfPresent(jobId) { _, current ->
            current.copy(status = "COMPLETED", totalChunks = totalChunks, processedChunks = totalChunks)
        }
    }

    fun failJob(jobId: String, error: String) {
        jobs.computeIfPresent(jobId) { _, current ->
            current.copy(status = "FAILED", errorMessage = error)
        }
    }

    fun getStatus(jobId: String): IngestionStatus? = jobs[jobId]

    fun getAllJobs(): List<IngestionStatus> = jobs.values.toList()
}
