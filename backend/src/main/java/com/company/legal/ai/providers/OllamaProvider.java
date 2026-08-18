package com.company.legal.ai.providers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Slf4j
@Component
public class OllamaProvider implements LlmProvider {

    @Value("${app.ai.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${app.ai.model:llama3.2}")
    private String defaultModel;

    private final WebClient webClient;

    public OllamaProvider() {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public String chat(String prompt) {
        log.info("Ollama chat request: {}", prompt.substring(0, Math.min(100, prompt.length())));
        return "AI response from " + defaultModel;
    }

    @Override
    public String chatWithContext(String prompt, String context) {
        String fullPrompt = "Context: " + context + "\n\nQuestion: " + prompt;
        return chat(fullPrompt);
    }

    @Override
    public String[] chatWithTools(String prompt, Map<String, Object> tools) {
        return new String[] { "Tool selection result" };
    }

    @Override
    public int[] countTokens(String text) {
        int tokens = text.length() / 4;
        return new int[] { tokens, tokens, tokens };
    }

    @Override
    public String getProviderName() {
        return "ollama";
    }

    @Override
    public String getDefaultModel() {
        return defaultModel;
    }
}