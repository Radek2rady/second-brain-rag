package org.example.secondbrainrag.domain

/**
 * Domain entity representing a single web search result.
 */
data class WebSearchResult(
    val title: String,
    val url: String,
    val content: String,
    val score: Double = 0.0
)
