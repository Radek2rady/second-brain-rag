package org.example.secondbrainrag.infrastructure

import jakarta.annotation.PostConstruct
import org.example.secondbrainrag.domain.FullTextSearchPort
import org.example.secondbrainrag.domain.VectorDocument
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@Component
class FullTextSearchAdapter(
    private val jdbcTemplate: JdbcTemplate
) : FullTextSearchPort {

    private val logger = LoggerFactory.getLogger(FullTextSearchAdapter::class.java)
    private val objectMapper = jacksonObjectMapper()

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
            val metadataStr = rs.getString("metadata") ?: "{}"
            val parsedMetadata = try {
                objectMapper.readValue<Map<String, String>>(metadataStr)
            } catch (e: Exception) {
                logger.warn("Failed to parse metadata JSON: {}", metadataStr)
                mapOf("raw" to metadataStr)
            }

            VectorDocument(
                id = rs.getString("id") ?: "",
                content = rs.getString("content") ?: "",
                metadata = parsedMetadata
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
     * Fallback search using ILIKE for substring matching.
     * Generates an OR-based SQL query utilizing up to the first 3 keywords to maximize recall.
     */
    private fun searchByIlike(query: String, topK: Int): List<VectorDocument> {
        val tokens = query.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(3)

        if (tokens.isEmpty()) {
            logger.warn("No valid tokens found for ILIKE fallback, returning empty list.")
            return emptyList()
        }

        // Build dynamic OR clause for up to 3 tokens
        val whereClause = tokens.joinToString(" OR ") { "content ILIKE ?" }
        val likePatterns = tokens.map { buildIlikePattern(it) }

        val sql = """
            SELECT id, content, metadata
            FROM vector_store
            WHERE $whereClause
            LIMIT ?
        """.trimIndent()

        // Prepare arguments: dynamic parameters followed by topK limit
        val args = likePatterns.toMutableList<Any>()
        args.add(topK)

        val results = jdbcTemplate.query(sql, { rs, _ ->
            val metadataStr = rs.getString("metadata") ?: "{}"
            val parsedMetadata = try {
                objectMapper.readValue<Map<String, String>>(metadataStr)
            } catch (e: Exception) {
                logger.warn("Failed to parse metadata JSON: {}", metadataStr)
                mapOf("raw" to metadataStr)
            }

            VectorDocument(
                id = rs.getString("id") ?: "",
                content = rs.getString("content") ?: "",
                metadata = parsedMetadata
            )
        }, *args.toTypedArray())

        logger.info("ILIKE fallback returned {} results for tokens={} using patterns={}", results.size, tokens, likePatterns)
        return results
    }

    /**
     * Builds an ILIKE pattern highly resilient to whitespace variations.
     * "§ 2165" → "%§%2165%"
     */
    private fun buildIlikePattern(keyword: String): String {
        // Split by whitespace and wrap with %
        val tokens = keyword.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        return if (tokens.size > 1) {
            "%" + tokens.joinToString("%") + "%"
        } else {
            "%${keyword.trim()}%"
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

        // Join with | for OR semantics.
        // The SQL query already uses ts_rank(content_tsv, to_tsquery(...)) DESC to prioritize chunks containing MORE of these terms.
        return expandedTokens.joinToString(" | ")
    }
}
