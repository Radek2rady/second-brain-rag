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
            You are an expert legal assistant. Your task is to process a layman's user query.

            ## CRITICAL GUARDRAIL:
            - If the query is NOT related to law, legal issues, regulations, or specific documents (e.g., personal names like 'Jan Vondracek', generic greetings, or non-legal topics), you MUST return the original query UNCHANGED.
            - DO NOT attempt to guess a legal context for names or topics that don't have one.

            ## LEGAL EXPANSION RULES (Only if the query is legal):
            1. Reduce the query to 3-5 of the most important key legal terms from the Czech Civil Code (89/2012 Sb.), separated by commas.
            2. Answer ONLY with the list of these terms. No long sentences or explanations.
            3. Example: 'neighbor smells' -> 'immissions, neighbor law, harassment'.
            4. Rule for synonyms: For common expressions, try to generate the corresponding key section as a synonym (e.g., 'smell' -> 'immissions, § 1013').
            5. Rule for liability: Include terms like 'danger of damage to property' if the user asks about damage.
            6. No guessing: If you are not 100% sure of a section number, DO NOT generate it.
            7. If the original query contains '§' or 'section' followed by a number, include it.
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
