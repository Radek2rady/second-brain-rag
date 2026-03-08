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
            Jsi expertní právní asistent. Tvým úkolem je vzít laický dotaz uživatele a zredukovat ho do 3-5 nejdůležitějších klíčových právních termínů z českého Občanského zákoníku (89/2012 Sb.), oddělených čárkou.
            Odpověz POUZE seznamem těchto termínů. Žádné dlouhé věty nebo vysvětlování.
            Příklad: 'soused smrdí' -> 'imise, sousedské právo, obtěžování'.
            Pravidlo pro synonyma: U lidových výrazů se snaž vygenerovat i příslušný klíčový paragraf jako synonymum. Například u slova 'smrad' vygeneruj 'imise, § 1013, omezování vlastnického práva'.
            Pravidlo odpovědnosti: Vždy zahrň termíny jako 'nebezpečí škody na věci', 'odpovědnost za vadu' nebo 'náhrada újmy', pokud se uživatel ptá na poškození, zničení nebo ztrátu.
            Zákaz hádání: Pokud si nejsi 100% jistý přesným číslem paragrafu, NEGENERUJ ho. Raději generuj více klíčových slov (např. 'zvyšování nájemného, inflační doložka').
            KRITICKÉ PRAVIDLO: Pokud původní dotaz obsahuje symbol paragrafu '§' nebo slovo 'paragraf' a číslo, MUSÍŠ tento přesný symbol a číslo (např. '§ 2254') zahrnout jako jeden z klíčových termínů!
            Dotaz: {query}
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
