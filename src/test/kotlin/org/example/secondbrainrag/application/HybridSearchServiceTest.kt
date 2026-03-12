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
    val rerankPort = mockk<org.example.secondbrainrag.domain.RerankPort>()
    val hybridSearchService = HybridSearchService(vectorDocumentPort, fullTextSearchPort, rerankPort)

    Given("a HybridSearchService with mocked ports") {

        When("a search query is executed") {
            val query = "§ 2165 reklamace"
            val expandedQuery = "§ 2165 reklamace rozšířená verze"
            val ftDoc = VectorDocument(id = "ft-1", content = "§ 2165 Kupující je oprávněn uplatnit...")
            val vectorDoc1 = VectorDocument(id = "vec-1", content = "Nějaký sémanticky podobný dokument...")
            val duplicateDoc = VectorDocument(id = "ft-1", content = "§ 2165 Kupující je oprávněn uplatnit...")
            
            
            // Expected to be called with candidateLimit=20
            every { fullTextSearchPort.searchByKeyword(expandedQuery, 20, "test-tenant") } returns listOf(ftDoc)
            every { vectorDocumentPort.searchSimilar(expandedQuery, 20, "test-tenant") } returns listOf(vectorDoc1, duplicateDoc)

            // Mock reranking - should be called with ORIGINAL query, not expandedQuery
            every { rerankPort.rerank(query, any()) } returns listOf(
                org.example.secondbrainrag.domain.RerankedDocument(ftDoc, 0.95),
                org.example.secondbrainrag.domain.RerankedDocument(vectorDoc1, 0.8)
            )

            val results = hybridSearchService.search(query, expandedQuery, tenantId = "test-tenant")

            Then("it should query ports, merge candidates, and rerank them using ORIGINAL query") {
                results.size shouldBe 2
                results[0].id shouldBe "ft-1"
                results[1].id shouldBe "vec-1"
                
                verify(exactly = 1) { fullTextSearchPort.searchByKeyword(expandedQuery, 20, "test-tenant") }
                verify(exactly = 1) { vectorDocumentPort.searchSimilar(expandedQuery, 20, "test-tenant") }
                verify(exactly = 1) { rerankPort.rerank(query, any()) } // Check original query
            }
        }

        When("reranking score is just above lower threshold") {
            val query = "marginal relevance query"
            val doc = VectorDocument(id = "marg-1", content = "Marginal relevance")

            every { fullTextSearchPort.searchByKeyword(query, 20, "test-tenant") } returns emptyList()
            every { vectorDocumentPort.searchSimilar(query, 20, "test-tenant") } returns listOf(doc)
            every { rerankPort.rerank(query, any()) } returns listOf(
                org.example.secondbrainrag.domain.RerankedDocument(doc, 0.06) // above 0.05
            )

            val results = hybridSearchService.search(query, query, tenantId = "test-tenant")

            Then("it should return the result (0.06 > 0.05)") {
                results.size shouldBe 1
                results[0].id shouldBe "marg-1"
            }
        }

        When("reranking score is below lower threshold") {
            val query = "very low score query"
            val doc = VectorDocument(id = "low-1", content = "Very low relevance")

            every { fullTextSearchPort.searchByKeyword(query, 20, "test-tenant") } returns emptyList()
            every { vectorDocumentPort.searchSimilar(query, 20, "test-tenant") } returns listOf(doc)
            every { rerankPort.rerank(query, any()) } returns listOf(
                org.example.secondbrainrag.domain.RerankedDocument(doc, 0.04) // below 0.05
            )

            val results = hybridSearchService.search(query, query, tenantId = "test-tenant")

            Then("it should return empty list") {
                results.size shouldBe 0
            }
        }
    }
})
