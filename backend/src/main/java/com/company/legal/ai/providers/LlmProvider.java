package com.company.legal.ai.providers;

import java.util.Map;

/**
 * Interface for LLM providers.
 */
public interface LlmProvider {
    String chat(String prompt);
    String chatWithContext(String prompt, String context);
    String[] chatWithTools(String prompt, Map<String, Object> tools);
    int[] countTokens(String text);
    String getProviderName();
    String getDefaultModel();

    /**
     * Whether the provider can be reached right now.
     */
    default boolean isAvailable() {
        return true;
    }
}