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
            You are an expert legal assistant. Your task is to take a layman's user query and reduce it to 3-5 of the most important key legal terms from the Czech Civil Code (89/2012 Sb.), separated by commas.
            Answer ONLY with the list of these terms. No long sentences or explanations.
            Example: 'neighbor smells' -> 'immissions, neighbor law, harassment'.
            Rule for synonyms: For common expressions, try to generate the corresponding key section as a synonym. For example, for the word 'smell', generate 'immissions, § 1013, restriction of property rights'.
            Rule for liability: Always include terms like 'danger of damage to property', 'liability for defect', or 'compensation for harm' if the user asks about damage, destruction, or loss.
            No guessing: If you are not 100% sure of the exact section number, DO NOT generate it. Instead, generate more keywords (e.g., 'rent increase, inflation clause').
            CRITICAL RULE: If the original query contains the section symbol '§' or the word 'section' followed by a number, you MUST include this exact symbol and number (e.g., '§ 2254') as one of the key terms!
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
