package org.example.secondbrainrag.application

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.secondbrainrag.domain.FullTextSearchPort
import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort

class HybridSearchServiceTest : BehaviorSpec({

    val vectorDocumentPort = mockk<VectorDocumentPort>()
    val fullTextSearchPort = mockk<FullTextSearchPort>()
    val legalQueryExpander = mockk<LegalQueryExpander>()
    val hybridSearchService = HybridSearchService(vectorDocumentPort, fullTextSearchPort, legalQueryExpander)

    Given("a HybridSearchService with mocked ports") {

        When("a search query is executed") {
            val query = "§ 2165 reklamace"
            val expandedQuery = "§ 2165 reklamace rozšířená verze"
            val ftDoc = VectorDocument(id = "ft-1", content = "§ 2165 Kupující je oprávněn uplatnit...")
            val vectorDoc1 = VectorDocument(id = "vec-1", content = "Nějaký sémanticky podobný dokument...")
            val duplicateDoc = VectorDocument(id = "ft-1", content = "§ 2165 Kupující je oprávněn uplatnit...")
            
            // Mock the expansion
            every { legalQueryExpander.expandQuery(query) } returns expandedQuery
            
            // Expected to be called with topK=15
            every { fullTextSearchPort.searchByKeyword(expandedQuery, 15, "test-tenant") } returns listOf(ftDoc)
            every { vectorDocumentPort.searchSimilar(expandedQuery, 15, "test-tenant") } returns listOf(vectorDoc1, duplicateDoc)

            val results = hybridSearchService.search(query, tenantId = "test-tenant")

            Then("it should query both full-text and vector ports and merge deduplicated results") {
                results.size shouldBe 2
                results[0].id shouldBe "ft-1" // fulltext has priority
                results[1].id shouldBe "vec-1"
                
                // Verify both ports and expander were called
                verify(exactly = 1) { legalQueryExpander.expandQuery(query) }
                verify(exactly = 1) { fullTextSearchPort.searchByKeyword(expandedQuery, 15, "test-tenant") }
                verify(exactly = 1) { vectorDocumentPort.searchSimilar(expandedQuery, 15, "test-tenant") }
            }
        }

        When("full-text returns empty results") {
            val query = "obecný dotaz"
            val vectorDoc = VectorDocument(id = "vec-1", content = "Nějaký text")

            every { legalQueryExpander.expandQuery(query) } returns query
            every { fullTextSearchPort.searchByKeyword(query, 15, "test-tenant") } returns emptyList()
            every { vectorDocumentPort.searchSimilar(query, 15, "test-tenant") } returns listOf(vectorDoc)

            val results = hybridSearchService.search(query, tenantId = "test-tenant")

            Then("it should return only vector results") {
                results.size shouldBe 1
                results[0].id shouldBe "vec-1"
            }
        }
    }
})
