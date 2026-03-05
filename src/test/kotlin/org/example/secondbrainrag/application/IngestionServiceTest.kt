package org.example.secondbrainrag.application

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.example.secondbrainrag.domain.DocumentSplitterPort
import org.example.secondbrainrag.domain.VectorDocument
import org.example.secondbrainrag.domain.VectorDocumentPort
import java.util.UUID

class IngestionServiceTest : BehaviorSpec({

    val splitterPort = mockk<DocumentSplitterPort>()
    val vectorDocumentPort = mockk<VectorDocumentPort>()
    val ingestionService = IngestionService(splitterPort, vectorDocumentPort)

    Given("A long text to ingest") {
        val longText = "a".repeat(1000)
        val metadata = mapOf("source" to "test_book")

        When("ingest() is called") {
            val chunk1 = "a".repeat(500)
            val chunk2 = "a".repeat(500)
            val expectedChunks = listOf(chunk1, chunk2)

            // Mock the splitter to return two chunks
            every { splitterPort.splitText(longText) } returns expectedChunks

            // Capture the list of documents passed to saveAll
            val capturedDocuments = slot<List<VectorDocument>>()
            every { vectorDocumentPort.saveAll(capture(capturedDocuments)) } returns Unit

            ingestionService.ingest(longText, metadata)

            Then("it should call splitText on the DocumentSplitterPort") {
                verify(exactly = 1) { splitterPort.splitText(longText) }
            }

            Then("it should call saveAll on the VectorDocumentPort") {
                verify(exactly = 1) { vectorDocumentPort.saveAll(any()) }
            }

            Then("the saved documents should have valid UUIDs, correct content and metadata") {
                val savedDocs = capturedDocuments.captured
                
                savedDocs.size shouldBe 2
                
                savedDocs[0].content shouldBe chunk1
                savedDocs[0].metadata shouldBe metadata
                // Verify UUID format - UUID.fromString throws IllegalArgumentException if invalid
                runCatching { UUID.fromString(savedDocs[0].id) }.isSuccess shouldBe true
                
                savedDocs[1].content shouldBe chunk2
                savedDocs[1].metadata shouldBe metadata
                runCatching { UUID.fromString(savedDocs[1].id) }.isSuccess shouldBe true
                
                // IDs should be unique
                (savedDocs[0].id != savedDocs[1].id) shouldBe true
            }
        }
    }
})
