package org.example.secondbrainrag.application

import org.example.secondbrainrag.domain.DocumentSplitterPort
import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort
import org.springframework.stereotype.Service

@Service
class IngestionService(
    private val splitterPort: DocumentSplitterPort,
    private val vectorDocumentPort: VectorDocumentPort
) {

    /**
     * Ingests a raw large text, splits it into chunks, and saves them to the vector store.
     */
    fun ingest(content: String, metadata: Map<String, String> = emptyMap()) {
        val chunks = splitterPort.splitText(content)
        
        val documents = chunks.map { chunkContent ->
            VectorDocument(
                content = chunkContent,
                metadata = metadata
            )
        }
        
        vectorDocumentPort.saveAll(documents)
    }
}
