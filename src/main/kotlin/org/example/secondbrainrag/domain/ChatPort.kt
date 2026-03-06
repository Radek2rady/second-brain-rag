package org.example.secondbrainrag.domain

/**
 * Port (Interface) for Generation AI capabilities.
 * Belongs to the Domain layer and acts as the boundary for LLM interactions.
 */
interface ChatPort {
    /**
     * Generates a conversational response based on the query, context, and chat history.
     * 
     * @param query The user's question
     * @param context The text context retrieved from the database
     * @param history Previous messages in the conversation
     * @param sourceHint Hint about the origin of the context ("LOCAL", "WEB", or "HYBRID")
     * @return Generated AI response
     */
    fun generateResponse(query: String, context: String, history: List<ChatMessage> = emptyList(), sourceHint: String = "LOCAL"): String
}
