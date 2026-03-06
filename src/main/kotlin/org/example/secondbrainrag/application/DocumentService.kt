package org.example.secondbrainrag.application

import org.example.secondbrainrag.domain.*
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.util.UUID

@Service
class DocumentService(
    private val vectorDocumentPort: VectorDocumentPort,
    private val chatPort: ChatPort,
    private val chatHistoryPort: ChatHistoryPort,
    private val webSearchPort: WebSearchPort
) {

    private val logger = LoggerFactory.getLogger(DocumentService::class.java)

    fun saveDocuments(documents: List<VectorDocument>) {
        vectorDocumentPort.save(documents)
    }

    fun searchSimilar(query: String, topK: Int = 4): List<VectorDocument> {
        return vectorDocumentPort.searchSimilar(query, topK)
    }

    fun chat(query: String, conversationId: String?): ChatResponseDomain {
        val activeConversationId = conversationId ?: UUID.randomUUID().toString()

        // 1. Retrieve history
        val history = chatHistoryPort.getLastMessages(activeConversationId, limit = 10)

        // 2. Retrieve relevant local documents
        val similarDocuments = searchSimilar(query)
        val hasLocalResults = similarDocuments.isNotEmpty()
        logger.info("chat query='{}': {} local documents found (hasLocalResults={})", query, similarDocuments.size, hasLocalResults)

        // 3. Determine if we need web search (0 local results = trigger web search)
        var webResults: List<WebSearchResult> = emptyList()
        if (!hasLocalResults) {
            logger.info("No local documents found for query='$query', falling back to web search")
            webResults = try {
                webSearchPort.search(query)
            } catch (e: Exception) {
                logger.warn("Web search failed for query='$query': ${e.message}")
                emptyList()
            }
        }

        val hasWebResults = webResults.isNotEmpty()

        // 4. Determine source
        val source = when {
            hasLocalResults && hasWebResults -> AnswerSource.HYBRID
            hasWebResults -> AnswerSource.WEB
            else -> AnswerSource.LOCAL
        }

        // 5. Build context
        val localContext = if (hasLocalResults) {
            similarDocuments.joinToString(separator = "\n\n") { it.content }
        } else ""

        val webContext = if (hasWebResults) {
            webResults.joinToString(separator = "\n\n") { "[${it.title}] (${it.url}): ${it.content}" }
        } else ""

        val context = when (source) {
            AnswerSource.LOCAL -> localContext
            AnswerSource.WEB -> webContext
            AnswerSource.HYBRID -> "=== LOKÁLNÍ DOKUMENTY ===\n$localContext\n\n=== WEBOVÉ ZDROJE ===\n$webContext"
        }

        // 6. Build references from web results
        val references = webResults.map { it.url }

        // 7. Generate response with source hint for transparency
        val sourceHint = source.name
        val response = chatPort.generateResponse(query, context, history, sourceHint)

        // 8. Save history
        chatHistoryPort.saveMessage(activeConversationId, ChatMessage(role = "user", content = query))
        chatHistoryPort.saveMessage(activeConversationId, ChatMessage(role = "assistant", content = response))

        return ChatResponseDomain(
            conversationId = activeConversationId,
            answer = response,
            source = source,
            references = references
        )
    }
}
