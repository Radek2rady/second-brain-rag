package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.VectorDocument
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@Import(VectorDocumentAdapterAccessControlTest.FakeEmbeddingConfig::class)
class VectorDocumentAdapterAccessControlTest {

    @TestConfiguration
    class FakeEmbeddingConfig {
        @Bean
        @Primary
        fun fakeEmbeddingModel(): org.springframework.ai.embedding.EmbeddingModel {
            return object : org.springframework.ai.embedding.EmbeddingModel {

                override fun call(request: org.springframework.ai.embedding.EmbeddingRequest): org.springframework.ai.embedding.EmbeddingResponse {
                    val embeddings = request.instructions.mapIndexed { index, _ ->
                        org.springframework.ai.embedding.Embedding(FloatArray(1536) { 0.1f }, index)
                    }
                    return org.springframework.ai.embedding.EmbeddingResponse(embeddings)
                }

                override fun embed(document: org.springframework.ai.document.Document): FloatArray {
                    return FloatArray(1536) { 0.1f }
                }

                override fun embed(text: String): FloatArray {
                    return FloatArray(1536) { 0.1f }
                }

                override fun embed(texts: MutableList<String>): MutableList<FloatArray> {
                    return texts.map { FloatArray(1536) { 0.1f } }.toMutableList()
                }
            }
        }
    }

    @Autowired
    private lateinit var vectorDocumentAdapter: VectorDocumentAdapter

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var cohereRerankAdapter: org.example.secondbrainrag.infrastructure.CohereRerankAdapter

    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var springAiChatAdapter: org.example.secondbrainrag.infrastructure.SpringAiChatAdapter

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("TRUNCATE TABLE vector_store")
    }

    @Test
    fun `should retrieve only PRIVATE docs for matching tenant and all COMPANY and GLOBAL docs`() {
        // Arrange
        val aliceTenant = "alice"
        val bobTenant = "bob"

        // 1. Alice's Private Document
        val alicePrivateDoc = VectorDocument(
            id = UUID.randomUUID().toString(),
            content = "Alice Secret Project X",
            metadata = mapOf("access_level" to "PRIVATE", "fileName" to "alice_secret.txt")
        )
        // 2. Bob's Private Document
        val bobPrivateDoc = VectorDocument(
            id = UUID.randomUUID().toString(),
            content = "Bob Secret Project Y",
            metadata = mapOf("access_level" to "PRIVATE", "fileName" to "bob_secret.txt")
        )
        // 3. Company Document (Uploaded by Bob but accessible to all)
        val companyDoc = VectorDocument(
            id = UUID.randomUUID().toString(),
            content = "Company Holiday Schedule",
            metadata = mapOf("access_level" to "COMPANY", "fileName" to "holidays.pdf")
        )
        // 4. Global Document (Uploaded by Admin but accessible to all)
        val globalDoc = VectorDocument(
            id = UUID.randomUUID().toString(),
            content = "Laws of Physics",
            metadata = mapOf("access_level" to "GLOBAL", "fileName" to "physics.pdf")
        )

        // Save documents
        vectorDocumentAdapter.saveAll(listOf(alicePrivateDoc), aliceTenant)
        vectorDocumentAdapter.saveAll(listOf(bobPrivateDoc, companyDoc), bobTenant)
        vectorDocumentAdapter.saveAll(listOf(globalDoc), "admin")

        // Act - Fetch all documents as Alice
        val aliceDocs = vectorDocumentAdapter.getAllDocuments(aliceTenant)
        // Act - Fetch all documents as Bob
        val bobDocs = vectorDocumentAdapter.getAllDocuments(bobTenant)

        // Assert for Alice
        assertEquals(3, aliceDocs.size, "Alice should see her private, company, and global docs")
        assertTrue(aliceDocs.any { it.content.contains("Alice Secret") }, "Alice should see her own document")
        assertTrue(aliceDocs.any { it.content.contains("Company Holiday") }, "Alice should see company documents")
        assertTrue(aliceDocs.any { it.content.contains("Laws of Physics") }, "Alice should see global documents")
        assertFalse(aliceDocs.any { it.content.contains("Bob Secret") }, "Alice MUST NOT see Bob's private document")

        // Assert for Bob
        assertEquals(3, bobDocs.size, "Bob should see his private, company, and global docs")
        assertTrue(bobDocs.any { it.content.contains("Bob Secret") }, "Bob should see his own document")
        assertTrue(bobDocs.any { it.content.contains("Company Holiday") }, "Bob should see company documents")
        assertTrue(bobDocs.any { it.content.contains("Laws of Physics") }, "Bob should see global documents")
        assertFalse(bobDocs.any { it.content.contains("Alice Secret") }, "Bob MUST NOT see Alice's private document")
    }

    @Test
    fun `hybrid search should filter out other tenant private documents`() {
        // Arrange
        val aliceTenant = "alice"
        val bobTenant = "bob"

        val aliceDoc = VectorDocument(
            id = UUID.randomUUID().toString(),
            content = "Important financial report for Alice",
            metadata = mapOf("access_level" to "PRIVATE")
        )
        val bobDoc = VectorDocument(
            id = UUID.randomUUID().toString(),
            content = "Important financial report for Bob",
            metadata = mapOf("access_level" to "PRIVATE")
        )
        val companyDoc = VectorDocument(
            id = UUID.randomUUID().toString(),
            content = "Important financial report for Company",
            metadata = mapOf("access_level" to "COMPANY")
        )

        vectorDocumentAdapter.saveAll(listOf(aliceDoc), aliceTenant)
        vectorDocumentAdapter.saveAll(listOf(bobDoc, companyDoc), bobTenant)

        // Act - Search as Alice for "financial report"
        val searchResults = vectorDocumentAdapter.searchSimilar("financial report", topK = 10, tenantId = aliceTenant)

        // Assert
        assertTrue(searchResults.isNotEmpty(), "Should find some results")
        assertTrue(searchResults.any { it.content.contains("Alice") }, "Should find Alice's report")
        assertTrue(searchResults.any { it.content.contains("Company") }, "Should find Company report")
        assertFalse(searchResults.any { it.content.contains("Bob") }, "Should strictly NOT find Bob's report via vector search")
    }
}