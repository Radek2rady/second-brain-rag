package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.WebSearchPort
import org.example.secondbrainrag.domain.WebSearchResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Mock implementation of WebSearchPort for UI testing without an API key.
 * Returns realistic-looking fake results so the full pipeline can be tested end-to-end.
 *
 * Replace with TavilyWebSearchAdapter when a real API key is available.
 */
@Component
class MockWebSearchAdapter : WebSearchPort {

    private val logger = LoggerFactory.getLogger(MockWebSearchAdapter::class.java)

    override fun search(query: String, maxResults: Int): List<WebSearchResult> {
        logger.info("MockWebSearchAdapter: performing mock web search for query='$query'")

        // Simulate a small delay like a real API would have
        Thread.sleep(200)

        return listOf(
            WebSearchResult(
                title = "Wikipedia: $query",
                url = "https://cs.wikipedia.org/wiki/${query.replace(" ", "_")}",
                content = "Toto je mockovaný výsledek z webu pro dotaz: \"$query\". " +
                        "V produkčním prostředí bude tento text nahrazen skutečným obsahem z webového vyhledávání (Tavily API). " +
                        "Tento výsledek slouží pouze k testování UI a transparentního zobrazení zdrojů.",
                score = 0.85
            ),
            WebSearchResult(
                title = "Odpovědi.cz – $query",
                url = "https://www.odpovedi.cz/otazky/${query.replace(" ", "-").lowercase()}",
                content = "Druhý mockovaný výsledek pro dotaz \"$query\". " +
                        "Obsahuje simulovaný kontext, který by v reálném prostředí pocházel z webového zdroje.",
                score = 0.72
            ),
            WebSearchResult(
                title = "Stack Overflow – $query",
                url = "https://stackoverflow.com/search?q=${query.replace(" ", "+")}",
                content = "Třetí mockovaný výsledek. Simuluje technický zdroj ze Stack Overflow pro testovací účely.",
                score = 0.65
            )
        ).take(maxResults)
    }
}
