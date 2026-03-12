package org.example.secondbrainrag.application

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.secondbrainrag.domain.*

class DocumentServiceTest : BehaviorSpec({

    val vectorDocumentPort = mockk<VectorDocumentPort>()
    val chatPort = mockk<ChatPort>()
    val chatHistoryPort = mockk<ChatHistoryPort>(relaxed = true)
    val webSearchPort = mockk<WebSearchPort>()
    val hybridSearchService = mockk<HybridSearchService>()
    val legalQueryExpander = mockk<LegalQueryExpander>()
    val documentService = DocumentService(vectorDocumentPort, chatPort, chatHistoryPort, webSearchPort, hybridSearchService, legalQueryExpander)

    Given("a DocumentService with mocked ports") {

        When("chat is called and LOCAL documents are found") {
            val query = "Jaké je hlavní město Francie?"
            val expectedResponse = "Paříž."
            val tenant = "test-tenant"

            every { legalQueryExpander.expandQuery(query) } returns query
            every { chatHistoryPort.getLastMessages(any(), any()) } returns emptyList()
            every { hybridSearchService.search(any(), any(), tenantId = tenant) } returns listOf(
                VectorDocument(content = "Paříž...", metadata = mapOf("fileName" to "geo.pdf"))
            )
            every { chatPort.generateResponse(any(), any(), any(), any()) } returns expectedResponse

            val result = documentService.chat(query, null, tenant)

            Then("it should return source LOCAL and correct references") {
                result.source shouldBe AnswerSource.LOCAL
                result.references shouldBe listOf("geo.pdf")
                verify(exactly = 0) { webSearchPort.search(any(), any()) }
            }
        }

        When("chat is called and NO local documents are found") {
            val query = "Kdo vyhrál loni ligu?"
            val tenant = "test-tenant"

            every { legalQueryExpander.expandQuery(query) } returns query
            every { chatHistoryPort.getLastMessages(any(), any()) } returns emptyList()
            every { hybridSearchService.search(any(), any(), tenantId = tenant) } returns emptyList()
            every { webSearchPort.search(any(), any()) } returns listOf(
                WebSearchResult("Sport", "https://sport.cz", "Vyhrála Sparta", 0.9)
            )
            every { chatPort.generateResponse(any(), any(), any(), "WEB") } returns "Vyhrála Sparta."

            val result = documentService.chat(query, null, tenant)

            Then("it should fallback to WEB") {
                result.source shouldBe AnswerSource.WEB
                verify(exactly = 1) { webSearchPort.search(any(), any()) }
            }
        }

        When("Alice (admin) asks a query") {
            val query = "Tajné platy"
            val tenantAlice = "alice-uuid"

            every { legalQueryExpander.expandQuery(query) } returns query
            every { chatHistoryPort.getLastMessages(any(), any()) } returns emptyList()
            every { hybridSearchService.search(any(), any(), tenantId = tenantAlice) } returns listOf(
                VectorDocument(content = "Plat Alice je 100k", metadata = mapOf("fileName" to "platy.pdf"))
            )
            every { chatPort.generateResponse(any(), any(), any(), any()) } returns "Vidím tabulku platů."

            val result = documentService.chat(query, null, tenantAlice)

            Then("hybridSearch must be called with Alice's tenantId") {
                verify { hybridSearchService.search(any(), any(), tenantId = tenantAlice) }
                result.references shouldBe listOf("platy.pdf")
            }
        }

        When("Jan (user) tries to access documents he doesn't own") {
            val query = "Tajné dokumenty"
            val tenantJan = "jan-uuid"

            every { legalQueryExpander.expandQuery(query) } returns query
            every { chatHistoryPort.getLastMessages(any(), any()) } returns emptyList()
            every { hybridSearchService.search(any(), any(), tenantId = tenantJan) } returns emptyList()
            every { webSearchPort.search(any()) } returns emptyList()
            every { chatPort.generateResponse(any(), any(), any(), any()) } returns "Nic jsem nenašel."

            val result = documentService.chat(query, null, tenantJan)

            Then("Jan must NOT see any references") {
                verify { hybridSearchService.search(any(), any(), tenantId = tenantJan) }
                result.references shouldBe emptyList()
            }
        }

        When("NO documents are found and WEB search is disabled") {
            val query = "Jaký je tajný recept na koláč?"
            val tenant = "test-tenant"

            every { legalQueryExpander.expandQuery(query) } returns query
            every {
                hybridSearchService.search(any<String>(), any<String>(), tenantId = tenant)
            } returns emptyList()
            every { webSearchPort.search(any()) } returns emptyList()
            // Simulujeme, že LLM dostane prázdný kontext
            every { chatPort.generateResponse(query, "", any(), any()) } returns "Omlouvám se, ale v nahraných dokumentech jsem tuto informaci nenašel."

            val result = documentService.chat(query, null, tenant)

            Then("it should return a polite refusal instead of hallucinating") {
                result.answer shouldBe "Omlouvám se, ale v nahraných dokumentech jsem tuto informaci nenašel."
                result.source shouldBe AnswerSource.LOCAL
                result.references shouldBe emptyList()
            }
        }
    }
})