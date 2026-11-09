package com.company.legal.ai.service;

import com.company.legal.ai.providers.LlmProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI Service for legal assistant functionality.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final LlmProvider llmProvider;

    /**
     * Send a chat message to the AI.
     */
    public String chat(String message) {
        requireMessage(message);
        log.info("AI chat request via {}: {}", llmProvider.getProviderName(), abbreviate(message));
        return llmProvider.chat(message);
    }

    /**
     * Send a chat message with context.
     */
    public String chatWithContext(String message, String context) {
        requireMessage(message);
        if (context == null || context.isBlank()) {
            log.warn("Context-aware AI request arrived without context; answering without it");
            return chat(message);
        }
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

    /**
     * Whether the underlying provider is reachable right now.
     */
    public boolean isAvailable() {
        return llmProvider.isAvailable();
    }

    private static void requireMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message must not be blank");
        }
    }

    private static String abbreviate(String message) {
        return message.substring(0, Math.min(100, message.length()));
    }
}
