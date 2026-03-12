package org.example.secondbrainrag

import org.example.secondbrainrag.infrastructure.CohereRerankAdapter
import org.example.secondbrainrag.infrastructure.SpringAiChatAdapter
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class SecondBrainRagApplicationTests {

    @MockBean
    private lateinit var cohereRerankAdapter: CohereRerankAdapter

    @MockBean
    private lateinit var springAiChatAdapter: SpringAiChatAdapter

    @Test
    fun contextLoads() {
    }
}