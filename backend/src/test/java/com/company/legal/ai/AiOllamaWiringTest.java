package com.company.legal.ai;

import com.company.legal.ai.providers.LlmProvider;
import com.company.legal.ai.providers.OllamaProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bean-wiring proof for the default provider choice: with {@code app.ai.provider=ollama}
 * only {@link OllamaProvider} is live (the mock's conditional does not match), so the
 * context loads with a single {@link LlmProvider} and no network call happens at startup.
 */
@SpringBootTest(properties = "app.ai.provider=ollama")
@ActiveProfiles("test")
class AiOllamaWiringTest {

    @Autowired
    private List<LlmProvider> llmProviders;

    @Test
    @DisplayName("provider=ollama wires exactly one LlmProvider: OllamaProvider")
    void exactlyOneOllamaProvider() {
        assertEquals(1, llmProviders.size());
        assertInstanceOf(OllamaProvider.class, llmProviders.get(0));
    }
}
