package com.company.legal.ai.providers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Mock LLM provider, selected with {@code app.ai.provider=mock} (the {@code test} profile default).
 */
@Profile("test")
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock")
public class MockLlmProvider implements LlmProvider {

    @Override
    public String chat(String prompt) {
        return "[MOCK AI] This is a mock response for: " + prompt.substring(0, Math.min(50, prompt.length()));
    }

    @Override
    public String chatWithContext(String prompt, String context) {
        return "[MOCK AI] Context-aware response for: " + prompt.substring(0, Math.min(30, prompt.length()));
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String[] chatWithTools(String prompt, Map<String, Object> tools) {
        return new String[] { "MOCK_TOOL_CALL", "tool_result" };
    }

    @Override
    public int[] countTokens(String text) {
        return new int[] { text.length() / 4, text.length() / 4, text.length() / 4 };
    }

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public String getDefaultModel() {
        return "mock-model";
    }
}