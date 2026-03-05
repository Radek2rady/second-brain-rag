package org.example.secondbrainrag.domain

/**
 * Port (Interface) for Generation AI capabilities.
 * Belongs to the Domain layer and acts as the boundary for LLM interactions.
 */
interface ChatPort {
    /**
     * Generates a conversational response based on the query and provided context.
     * 
     * @param query The user's question
     * @param context The text context retrieved from the database
     * @return Generated AI response
     */
    fun generateResponse(query: String, context: String): String
}
