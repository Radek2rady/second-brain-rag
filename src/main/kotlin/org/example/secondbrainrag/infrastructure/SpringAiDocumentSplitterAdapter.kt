package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.DocumentSplitterPort
import org.springframework.stereotype.Component
import kotlin.math.min

@Component
class SpringAiDocumentSplitterAdapter : DocumentSplitterPort {

    companion object {
        private const val CHUNK_SIZE = 1500
        private const val OVERLAP = 250
        private val SENTENCE_ENDINGS = setOf('.', '?', '!', '\n')
    }

    override fun splitText(content: String): List<String> {
        // Pre-normalize the entire content before splitting
        val normalized = content
            .replace(Regex("[\\u200B\\u200C\\u200D\\uFEFF\\u00AD\\u2060]"), "") // strip invisible chars
            .replace(Regex("[\\t ]+"), " ") // collapse whitespace

        if (normalized.length <= CHUNK_SIZE) {
            return listOf(normalized.trim()).filter { it.isNotBlank() }
        }

        val chunks = mutableListOf<String>()
        var start = 0

        while (start < normalized.length) {
            var end = min(start + CHUNK_SIZE, normalized.length)

            // If we're not at the end of the text, try to find a sentence boundary
            if (end < normalized.length) {
                val searchStart = maxOf(end - 200, start) // Look back up to 200 chars for a sentence end
                var bestBreak = -1
                for (i in end - 1 downTo searchStart) {
                    if (normalized[i] in SENTENCE_ENDINGS) {
                        bestBreak = i + 1
                        break
                    }
                }
                if (bestBreak > start) {
                    end = bestBreak
                }
            }

            chunks.add(normalized.substring(start, end).trim())

            // Move start forward, accounting for overlap
            start = if (end >= normalized.length) {
                normalized.length // Done
            } else {
                maxOf(end - OVERLAP, start + 1)
            }
        }

        return chunks.filter { it.isNotBlank() }
    }
}
