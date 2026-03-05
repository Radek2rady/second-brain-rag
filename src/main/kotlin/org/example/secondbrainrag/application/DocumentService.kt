package org.example.secondbrainrag.application

import org.example.secondbrainrag.domain.ChatHistoryPort
import org.example.secondbrainrag.domain.ChatMessage
import org.example.secondbrainrag.domain.ChatPort
import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DocumentService(
    private val vectorDocumentPort: VectorDocumentPort,
    private val chatPort: ChatPort,
    private val chatHistoryPort: ChatHistoryPort
) {

    fun saveDocuments(documents: List<VectorDocument>) {
        vectorDocumentPort.save(documents)
    }

    fun searchSimilar(query: String, topK: Int = 4): List<VectorDocument> {
        return vectorDocumentPort.searchSimilar(query, topK)
    }

    fun chat(query: String, conversationId: String?): Pair<String, String> {
        val activeConversationId = conversationId ?: UUID.randomUUID().toString()

        // 1. Retrieve history
        val history = chatHistoryPort.getLastMessages(activeConversationId, limit = 10)

        // 2. Retrieve relevant documents
        val similarDocuments = searchSimilar(query)
        
        // 3. Combine document contents into context
        val context = similarDocuments.joinToString(separator = "\n\n") { it.content }
        
        // 4. Delegate generation to ChatPort within the Hexagonal Architecture
        val response = chatPort.generateResponse(query, context, history)

        // 5. Save history
        chatHistoryPort.saveMessage(activeConversationId, ChatMessage(role = "user", content = query))
        chatHistoryPort.saveMessage(activeConversationId, ChatMessage(role = "assistant", content = response))

        return Pair(activeConversationId, response)
    }
}
