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
    val documentService = DocumentService(vectorDocumentPort, chatPort, chatHistoryPort, webSearchPort)

    Given("a DocumentService with mocked ports") {

        When("chat is called and LOCAL documents are found") {
            val query = "Jaké je hlavní město Francie?"
            val expectedContext = "Paříž je hlavní město Francie.\n\nFrancie leží v Evropě."
            val expectedResponse = "Hlavním městem Francie je Paříž."

            every { chatHistoryPort.getLastMessages(any(), any()) } returns emptyList()
            every { vectorDocumentPort.searchSimilar(query, 4) } returns listOf(
                VectorDocument(content = "Paříž je hlavní město Francie."),
                VectorDocument(content = "Francie leží v Evropě.")
            )
            every { chatPort.generateResponse(query, expectedContext, emptyList(), "LOCAL") } returns expectedResponse

            val result = documentService.chat(query, null)

            Then("it should return source LOCAL") {
                result.source shouldBe AnswerSource.LOCAL
            }

            Then("it should NOT call web search") {
                verify(exactly = 0) { webSearchPort.search(any(), any()) }
            }

            Then("it should return the generated response") {
                result.answer shouldBe expectedResponse
            }

            Then("references should be empty") {
                result.references shouldBe emptyList()
            }
        }

        When("chat is called and NO local documents are found") {
            val query = "Co je kvantová gravitace?"
            val webContent = "[Wikipedia: kvantová gravitace] (https://example.com): Kvantová gravitace..."
            val expectedResponse = "Na základě informací z internetu..."

            every { chatHistoryPort.getLastMessages(any(), any()) } returns emptyList()
            every { vectorDocumentPort.searchSimilar(query, 4) } returns emptyList()
            every { webSearchPort.search(query, 3) } returns listOf(
                WebSearchResult(
                    title = "Wikipedia: kvantová gravitace",
                    url = "https://example.com",
                    content = "Kvantová gravitace...",
                    score = 0.9
                )
            )
            every { chatPort.generateResponse(query, any(), emptyList(), "WEB") } returns expectedResponse

            val result = documentService.chat(query, null)

            Then("it should return source WEB") {
                result.source shouldBe AnswerSource.WEB
            }

            Then("it should call web search") {
                verify(exactly = 1) { webSearchPort.search(query, 3) }
            }

            Then("references should contain the web URL") {
                result.references shouldBe listOf("https://example.com")
            }

            Then("it should return the web-based response") {
                result.answer shouldBe expectedResponse
            }
        }
    }
})
