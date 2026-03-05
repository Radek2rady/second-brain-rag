package org.example.secondbrainrag.domain

/**
 * Represents a single message within a chat conversation.
 */
data class ChatMessage(
    val role: String, // "user", "assistant", or "system"
    val content: String
)

/**
 * Port (Interface) for persisting and retrieving chat history.
 */
interface ChatHistoryPort {
    /**
     * Saves a new message to the history of a specific conversation.
     */
    fun saveMessage(conversationId: String, message: ChatMessage)

    /**
     * Retrieves the last N messages for a specific conversation.
     * Returned in chronological order (oldest first).
     */
    fun getLastMessages(conversationId: String, limit: Int): List<ChatMessage>

    /**
     * Retrieves a list of unique conversation IDs, ordered by the most recently created message.
     */
    fun getConversations(): List<String>
}
