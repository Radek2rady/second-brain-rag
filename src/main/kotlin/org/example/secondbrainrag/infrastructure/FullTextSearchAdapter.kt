package org.example.secondbrainrag.infrastructure

import jakarta.annotation.PostConstruct
import org.example.secondbrainrag.domain.FullTextSearchPort
import org.example.secondbrainrag.domain.VectorDocument
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory

@Component
class FullTextSearchAdapter(
    private val jdbcTemplate: JdbcTemplate
) : FullTextSearchPort {

    private val logger = LoggerFactory.getLogger(FullTextSearchAdapter::class.java)

    companion object {
        private val PARAGRAPH_REGEX = Regex("""§\s*\d+\w*""")
    }

    @PostConstruct
    fun initializeFullTextSearch() {
        logger.info("Initializing full-text search schema on vector_store table...")

        // Add tsvector column if it doesn't exist
        jdbcTemplate.execute("""
            DO ${'$'}${'$'}
            BEGIN
                IF NOT EXISTS (
                    SELECT 1 FROM information_schema.columns
                    WHERE table_name = 'vector_store' AND column_name = 'content_tsv'
                ) THEN
                    ALTER TABLE vector_store ADD COLUMN content_tsv tsvector;
                END IF;
            END
            ${'$'}${'$'};
        """.trimIndent())

        // Update existing rows that have NULL content_tsv
        jdbcTemplate.execute("""
            UPDATE vector_store
            SET content_tsv = to_tsvector('simple', coalesce(content, ''))
            WHERE content_tsv IS NULL;
        """.trimIndent())

        // Create GIN index if it doesn't exist
        jdbcTemplate.execute("""
            CREATE INDEX IF NOT EXISTS idx_vector_store_content_tsv
            ON vector_store USING GIN (content_tsv);
        """.trimIndent())

        // Create or replace trigger function
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION vector_store_tsv_trigger() RETURNS trigger AS ${'$'}${'$'}
            BEGIN
                NEW.content_tsv := to_tsvector('simple', coalesce(NEW.content, ''));
                RETURN NEW;
            END
            ${'$'}${'$'} LANGUAGE plpgsql;
        """.trimIndent())

        // Create trigger if it doesn't exist
        jdbcTemplate.execute("""
            DO ${'$'}${'$'}
            BEGIN
                IF NOT EXISTS (
                    SELECT 1 FROM pg_trigger WHERE tgname = 'trg_vector_store_tsv'
                ) THEN
                    CREATE TRIGGER trg_vector_store_tsv
                    BEFORE INSERT OR UPDATE ON vector_store
                    FOR EACH ROW
                    EXECUTE FUNCTION vector_store_tsv_trigger();
                END IF;
            END
            ${'$'}${'$'};
        """.trimIndent())

        logger.info("Full-text search schema initialized successfully.")
    }

    override fun searchByKeyword(query: String, topK: Int): List<VectorDocument> {
        // Extract paragraph reference if present to prevent searching for the whole sentence
        val paragraphMatch = PARAGRAPH_REGEX.find(query)
        val actualQuery = if (paragraphMatch != null) {
            val extracted = paragraphMatch.value
            logger.info("Extracted paragraph reference '{}' from original query '{}'", extracted, query)
            extracted
        } else {
            query
        }

        val tsQuery = buildTsQuery(actualQuery)
        logger.info("Full-text search: actualQuery='{}', tsQuery='{}'", actualQuery, tsQuery)

        if (tsQuery.isBlank()) {
            logger.info("Empty tsQuery, falling back to ILIKE search for actualQuery='{}'", actualQuery)
            return searchByIlike(actualQuery, topK)
        }

        val sql = """
            SELECT id, content, metadata
            FROM vector_store
            WHERE content_tsv @@ to_tsquery('simple', ?)
            ORDER BY ts_rank(content_tsv, to_tsquery('simple', ?)) DESC
            LIMIT ?
        """.trimIndent()

        val results = jdbcTemplate.query(sql, { rs, _ ->
            VectorDocument(
                id = rs.getString("id") ?: "",
                content = rs.getString("content") ?: "",
                metadata = mapOf("raw" to (rs.getString("metadata") ?: "{}"))
            )
        }, tsQuery, tsQuery, topK)

        logger.info("Full-text search returned {} results for actualQuery='{}'", results.size, actualQuery)

        // If tsvector returned nothing, always try ILIKE fallback
        if (results.isEmpty()) {
            logger.info("tsvector returned 0 results, falling back to ILIKE for actualQuery='{}'", actualQuery)
            return searchByIlike(actualQuery, topK)
        }

        return results
    }

    /**
     * Fallback search using ILIKE for exact substring matching.
     * Handles § spacing variations: "§ 2165", "§2165", "§  2165" all match.
     */
    private fun searchByIlike(query: String, topK: Int): List<VectorDocument> {
        // Build a pattern resilient to spacing between § and number
        val likePattern = buildIlikePattern(query)
        val sql = """
            SELECT id, content, metadata
            FROM vector_store
            WHERE content ILIKE ?
            LIMIT ?
        """.trimIndent()

        val results = jdbcTemplate.query(sql, { rs, _ ->
            VectorDocument(
                id = rs.getString("id") ?: "",
                content = rs.getString("content") ?: "",
                metadata = mapOf("raw" to (rs.getString("metadata") ?: "{}"))
            )
        }, likePattern, topK)

        logger.info("ILIKE fallback returned {} results for pattern='{}'", results.size, likePattern)
        return results
    }

    /**
     * Builds an ILIKE pattern that is resilient to whitespace variations.
     * "§ 2165" → "%§%2165%" (matches "§2165", "§ 2165", "§  2165")
     */
    private fun buildIlikePattern(query: String): String {
        // Split the query into meaningful tokens, use % between each for flexible matching
        val tokens = query.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        return if (tokens.size > 1) {
            "%" + tokens.joinToString("%") + "%"
        } else {
            "%${query.trim()}%"
        }
    }

    /**
     * Builds a tsquery string from the user's raw query.
     * Handles § symbols and multi-word queries using & (AND) operator.
     */
    private fun buildTsQuery(query: String): String {
        // Clean and tokenize: remove § (not indexed by 'simple' config), keep numbers and words
        val tokens = query
            .replace("§", "")
            .trim()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .map { it.replace(Regex("[^\\w]"), "") } // strip non-word chars
            .filter { it.isNotBlank() }

        val expandedTokens = mutableListOf<String>()
        for (t in tokens) {
            val lowerT = t.lowercase()
            if (lowerT.startsWith("reklamac")) {
                expandedTokens.add("(reklamace | vada | (vadné & plnění) | vytknout)")
            } else if (lowerT.startsWith("bazar")) {
                expandedTokens.add("(bazar | (použitá & věc) | (použité & věci))")
            } else if (lowerT.startsWith("záruk") || lowerT.startsWith("zaruk")) {
                expandedTokens.add("(záruka | jakost | vady)")
            } else {
                expandedTokens.add(t)
            }
        }

        if (expandedTokens.isEmpty()) return ""

        // Join with & for AND semantics
        return expandedTokens.joinToString(" & ")
    }
}
