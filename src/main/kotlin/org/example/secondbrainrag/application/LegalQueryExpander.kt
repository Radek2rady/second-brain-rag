package org.example.secondbrainrag.application

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

@Service
class LegalQueryExpander(
    builder: ChatClient.Builder
) {

    private val chatClient: ChatClient = builder.build()
    private val logger = LoggerFactory.getLogger(LegalQueryExpander::class.java)

    fun expandQuery(query: String): String {
        val systemPrompt = """
            You are an expert search query generator and intent classifier. 
            
            Analyze the user's query and classify it into one of three categories:
            
            1. META_QUERY (Asking about the database or available documents)
               If the user asks questions like "What documents are saved?", "What is in the database?", "Shrň nahrané dokumenty", or queries about the nature, content, or existence of the uploaded files, you MUST output EXACTLY the phrase:
               META_QUERY
            
            2. SPECIFIC LEGAL QUERY (Asking about Czech Civil Code 89/2012 Sb.)
               If the query is a specific legal question or describes a legal situation (e.g., 'neighbor smells', 'rent increase'):
               Reduce it to 3-5 of the most important key legal terms from the Czech Civil Code, separated by commas.
               Example: 'neighbor smells' -> 'immissions, neighbor law, harassment'.
               Rule for synonyms: For common expressions, try to generate the corresponding key section (e.g., 'smell' -> 'immissions, § 1013').
               Rule for liability: Include terms like 'danger of damage to property' if the user asks about damage.
               No guessing: If you are not 100% sure of a section number, DO NOT generate it.
               CRITICAL: If the original query contains '§' or 'section' followed by a number, include it.
            
            3. GENERAL NON-LEGAL QUERY (e.g., greeting, asking about a person like Jan Vondracek, general topic)
               If it is NOT a meta-query and NOT a legal query:
               Output 2-3 simple keywords in the language of the query that represent the core intent, optimized for search. Do not hallucinate legal terms!
            
            Answer ONLY with the classification result ('META_QUERY' or the comma-separated keywords).
            Query: {query}
        """.trimIndent()

        logger.info("Expanding layman query via LLM: '{}'", query)

        val expanded = try {
            chatClient.prompt()
                .system { s ->
                    s.text(systemPrompt)
                     .param("query", query)
                }
                .user(query)
                .call()
                .content()
        } catch (e: Exception) {
            logger.error("Error calling LLM for query expansion: {}", e.message)
            null
        }

        if (expanded.isNullOrBlank()) {
             logger.warn("Query expansion failed, returning the original query as fallback.")
             return query
        }

        logger.info("=== LEGAL QUERY EXPANDER DUMP ===")
        logger.info("Original Layman Query: '{}'", query)
        logger.info("Expanded Legal Query:  '{}'", expanded.trim())
        logger.info("===================================")
        return expanded.trim()
    }
}
