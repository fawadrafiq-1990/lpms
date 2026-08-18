package com.company.legal.ai.service;

import com.company.legal.ai.providers.LlmProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * AI Service for legal assistant functionality.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final LlmProvider llmProvider;

    @Value("${app.ai.model}")
    private String defaultModel;

    /**
     * Send a chat message to the AI.
     */
    public String chat(String message) {
        log.info("AI chat request: {}", message.substring(0, Math.min(100, message.length())));
        return llmProvider.chat(message);
    }

    /**
     * Send a chat message with context.
     */
    public String chatWithContext(String message, String context) {
        return llmProvider.chatWithContext(message, context);
    }

    /**
     * Get the current provider name.
     */
    public String getProvider() {
        return llmProvider.getProviderName();
    }

    /**
     * Get the current model.
     */
    public String getModel() {
        return llmProvider.getDefaultModel();
    }
}