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
    val hybridSearchService = HybridSearchService(vectorDocumentPort, fullTextSearchPort)

    Given("a HybridSearchService with mocked ports") {

        When("a search query is executed") {
            val query = "§ 2165 reklamace"
            val ftDoc = VectorDocument(id = "ft-1", content = "§ 2165 Kupující je oprávněn uplatnit...")
            val vectorDoc1 = VectorDocument(id = "vec-1", content = "Nějaký sémanticky podobný dokument...")
            val duplicateDoc = VectorDocument(id = "ft-1", content = "§ 2165 Kupující je oprávněn uplatnit...")
            
            // Expected to be called with topK=5
            every { fullTextSearchPort.searchByKeyword(query, 5) } returns listOf(ftDoc)
            every { vectorDocumentPort.searchSimilar(query, 5) } returns listOf(vectorDoc1, duplicateDoc)

            val results = hybridSearchService.search(query)

            Then("it should query both full-text and vector ports and merge deduplicated results") {
                results.size shouldBe 2
                results[0].id shouldBe "ft-1" // fulltext has priority
                results[1].id shouldBe "vec-1"
                
                // Verify both ports were called
                verify(exactly = 1) { fullTextSearchPort.searchByKeyword(query, 5) }
                verify(exactly = 1) { vectorDocumentPort.searchSimilar(query, 5) }
            }
        }

        When("full-text returns empty results") {
            val query = "obecný dotaz"
            val vectorDoc = VectorDocument(id = "vec-1", content = "Nějaký text")

            every { fullTextSearchPort.searchByKeyword(query, 5) } returns emptyList()
            every { vectorDocumentPort.searchSimilar(query, 5) } returns listOf(vectorDoc)

            val results = hybridSearchService.search(query)

            Then("it should return only vector results") {
                results.size shouldBe 1
                results[0].id shouldBe "vec-1"
            }
        }
    }
})
