package org.example.secondbrainrag.application

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.secondbrainrag.domain.ChatPort
import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort

class DocumentServiceTest : BehaviorSpec({

    val vectorDocumentPort = mockk<VectorDocumentPort>()
    val chatPort = mockk<ChatPort>()
    val documentService = DocumentService(vectorDocumentPort, chatPort)

    Given("a DocumentService with mocked ports") {
        
        When("chat(query) is called") {
            val query = "Jaké je hlavní město Francie?"
            val expectedContext = "Paříž je hlavní město Francie.\n\nFrancie leží v Evropě."
            val expectedResponse = "Hlavním městem Francie je Paříž."
            
            // Mocking the VectorDocumentPort Retrieval
            every { vectorDocumentPort.searchSimilar(query, 4) } returns listOf(
                VectorDocument(content = "Paříž je hlavní město Francie."),
                VectorDocument(content = "Francie leží v Evropě.")
            )
            
            // Mocking the ChatPort Generation
            every { chatPort.generateResponse(query, expectedContext) } returns expectedResponse

            val result = documentService.chat(query)

            Then("it should first retrieve context from VectorDocumentPort") {
                verify(exactly = 1) { vectorDocumentPort.searchSimilar(query, 4) }
            }

            Then("it should assemble the context and call generateResponse on ChatPort") {
                verify(exactly = 1) { chatPort.generateResponse(query, expectedContext) }
            }
            
            Then("it should return the final generated response") {
                result shouldBe expectedResponse
            }
        }
    }
})
