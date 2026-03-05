package org.example.secondbrainrag.domain

/**
 * Port (Interface) for splitting large documents into smaller logical chunks.
 */
interface DocumentSplitterPort {
    /**
     * Splits a single string of content into a list of smaller strings (chunks).
     */
    fun splitText(content: String): List<String>
}
