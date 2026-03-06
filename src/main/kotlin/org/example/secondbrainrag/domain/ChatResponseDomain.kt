package org.example.secondbrainrag.domain

/**
 * Indicates the origin of information used to generate a chat response.
 */
enum class AnswerSource {
    /** Answer is based solely on locally uploaded documents. */
    LOCAL,
    /** Answer is based on web search results (no local documents matched). */
    WEB,
    /** Answer combines both local documents and web search results. */
    HYBRID
}

/**
 * Domain model representing a complete chat response with source transparency metadata.
 */
data class ChatResponseDomain(
    val conversationId: String,
    val answer: String,
    val source: AnswerSource,
    val references: List<String> = emptyList()
)
