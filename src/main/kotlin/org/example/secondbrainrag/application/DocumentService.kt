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
    private val webSearchPort: WebSearchPort,
    private val hybridSearchService: HybridSearchService,
    private val legalQueryExpander: LegalQueryExpander
) {

    private val logger = LoggerFactory.getLogger(DocumentService::class.java)

    fun saveDocuments(documents: List<VectorDocument>, tenantId: String) {
        vectorDocumentPort.save(documents, tenantId)
    }

    fun searchSimilar(query: String, topK: Int = 4, tenantId: String): List<VectorDocument> {
        return vectorDocumentPort.searchSimilar(query, topK, tenantId)
    }

    fun chat(query: String, conversationId: String?, tenantId: String): ChatResponseDomain {
        val activeConversationId = conversationId ?: UUID.randomUUID().toString()

        // 1. Retrieve history
        val history = chatHistoryPort.getLastMessages(activeConversationId, tenantId, limit = 10)

        // 2. Classify Intent & Expand Query
        val expandedQuery = legalQueryExpander.expandQuery(query)
        val isMetaQuery = expandedQuery.equals("META_QUERY", ignoreCase = true)

        var similarDocuments: List<VectorDocument> = emptyList()
        var webResults: List<WebSearchResult> = emptyList()
        var localReferences: List<String> = emptyList()
        val source: AnswerSource
        val context: String

        if (isMetaQuery) {
            // META QUERY ROUTING: Bypass vector/hybrid search entirely
            logger.info("chat query='{}': Detected as META_QUERY. Fetching full database metadata.", query)
            val allFiles = vectorDocumentPort.findAllMetadata(tenantId)
            
            source = AnswerSource.LOCAL
            localReferences = allFiles
            context = if (allFiles.isNotEmpty()) {
                "=== DATABASE OVERVIEW ===\nUser's database contains the following uploaded files:\n" + 
                allFiles.joinToString(separator = "\n") { "- $it" }
            } else {
                "The database is currently empty. No documents have been uploaded."
            }
        } else {
            // STANDARD ROUTING: Retrieve relevant local documents via hybrid search
            similarDocuments = hybridSearchService.search(query, expandedQuery, tenantId = tenantId)
            val hasLocalResults = similarDocuments.isNotEmpty()
            logger.info("chat query='{}': {} local documents found (hasLocalResults={})", query, similarDocuments.size, hasLocalResults)

            // Determine if we need web search (0 local results = trigger web search)
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

            source = when {
                hasLocalResults && hasWebResults -> AnswerSource.HYBRID
                hasWebResults -> AnswerSource.WEB
                else -> AnswerSource.LOCAL
            }

            val localContext = if (hasLocalResults) {
                similarDocuments.joinToString(separator = "\n\n") {
                    val fileInfo = it.metadata["fileName"] ?: it.metadata["source"] ?: "Unknown File"
                    "--- SOURCE: $fileInfo ---\n${it.content}"
                }
            } else ""

            val webContext = if (hasWebResults) {
                webResults.joinToString(separator = "\n\n") { "[${it.title}] (${it.url}): ${it.content}" }
            } else ""

            context = when (source) {
                AnswerSource.LOCAL -> localContext
                AnswerSource.WEB -> webContext
                AnswerSource.HYBRID -> "=== LOCAL DOCUMENTS ===\n$localContext\n\n=== WEB SOURCES ===\n$webContext"
            }
            
            localReferences = similarDocuments.map { (it.metadata["fileName"] ?: it.metadata["source"] ?: "Unknown File").toString() }.distinct()
        }

        // Build final references
        val webReferences = webResults.map { it.url }
        val references = localReferences + webReferences

        // Generate response with source hint for transparency
        val sourceHint = source.name
        val response = chatPort.generateResponse(query, context, history, sourceHint)

        // 8. Save history
        chatHistoryPort.saveMessage(activeConversationId, tenantId, ChatMessage(role = "user", content = query))
        chatHistoryPort.saveMessage(activeConversationId, tenantId, ChatMessage(role = "assistant", content = response))

        return ChatResponseDomain(
            conversationId = activeConversationId,
            answer = response,
            source = source,
            references = references
        )
    }
}
