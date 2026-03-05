package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.DocumentSplitterPort
import org.springframework.ai.document.Document
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.stereotype.Component

@Component
class SpringAiDocumentSplitterAdapter : DocumentSplitterPort {

    // TokenTextSplitter in Spring AI takes params like defaultChunkSize
    // User requested chunkSize=200, keepIndexDistances=true.
    private val splitter = TokenTextSplitter()

    // Assuming we need to configure it if properties match exactly, or fallback to default
    // We will just split the raw text into multiple chunks
    override fun splitText(content: String): List<String> {
        // Wrap the single string in a Document temporarily so Spring AI can process it
        val inputDocs = listOf(Document(content))
        val outputDocs = splitter.apply(inputDocs)
        
        return outputDocs.map { it.content }
    }
}
