package com.company.legal.ai;

import com.company.legal.ai.providers.LlmProvider;
import com.company.legal.ai.providers.MockLlmProvider;
import com.company.legal.ai.service.AIService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bean-wiring proof: under the {@code test} profile ({@code app.ai.provider=mock})
 * exactly one {@link LlmProvider} exists, so the context cannot fail with
 * {@code NoUniqueBeanDefinitionException}.
 */
@SpringBootTest
@ActiveProfiles("test")
class AiMockWiringTest {

    @Autowired
    private List<LlmProvider> llmProviders;

    @Autowired
    private AIService aiService;

    @Test
    @DisplayName("test profile wires exactly one LlmProvider: the mock")
    void exactlyOneMockProvider() {
        assertEquals(1, llmProviders.size());
        assertInstanceOf(MockLlmProvider.class, llmProviders.get(0));
    }

    @Test
    @DisplayName("AIService answers through the wired mock provider")
    void serviceUsesMockProvider() {
        assertTrue(aiService.chat("ping").startsWith("[MOCK AI]"));
        assertEquals("mock", aiService.getProvider());
        assertTrue(aiService.isAvailable());
    }
}
