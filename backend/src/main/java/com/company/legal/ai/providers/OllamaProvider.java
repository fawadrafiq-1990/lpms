package com.company.legal.ai.providers;

import com.company.legal.ai.exception.AiProviderException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ollama-backed {@link LlmProvider}, using Ollama's native REST API:
 * {@code POST /api/chat} for completions and {@code GET /api/tags} for availability.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaProvider implements LlmProvider {

    private static final String SYSTEM_PROMPT = """
            You are a legal practice management assistant. Answer using only the supplied context. \
            If the context does not contain the answer, say that the context is insufficient. \
            Never invent case law, statute citations, dates or party names.""";
    private static final int CHARS_PER_TOKEN_ESTIMATE = 4;
    private static final long AVAILABILITY_PROBE_TIMEOUT_MS = 3_000L;
    private static final long BLOCK_GRACE_MS = 5_000L;

    private final String defaultModel;
    private final double temperature;
    private final int maxTokens;
    private final long timeoutMs;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OllamaProvider(
            @Value("${app.ai.base-url:http://localhost:11434}") String baseUrl,
            @Value("${app.ai.model:llama3.2}") String defaultModel,
            @Value("${app.ai.api-key:}") String apiKey,
            @Value("${app.ai.temperature:0.7}") double temperature,
            @Value("${app.ai.max-tokens:4096}") int maxTokens,
            @Value("${app.ai.timeout:120000}") long timeoutMs,
            ObjectMapper objectMapper) {
        this.defaultModel = defaultModel;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.timeoutMs = timeoutMs;
        this.objectMapper = objectMapper;

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        }
        this.webClient = builder.build();
    }

    @Override
    public String chat(String prompt) {
        return complete(List.of(message("user", prompt)));
    }

    @Override
    public String chatWithContext(String prompt, String context) {
        String system = SYSTEM_PROMPT + "\n\nContext:\n" + truncateContext(context);
        return complete(List.of(message("system", system), message("user", prompt)));
    }

    /**
     * Sends the request with tool definitions and returns each tool call Ollama emitted,
     * serialized as {@code {"name": "...", "arguments": {...}}}.
     *
     * @param tools function name to definition (usually {@code description} and {@code parameters});
     *              the object is inlined into the {@code function} node of the tool definition.
     */
    @Override
    public String[] chatWithTools(String prompt, Map<String, Object> tools) {
        Map<String, Object> body = baseBody(List.of(message("user", prompt)));
        if (tools != null && !tools.isEmpty()) {
            body.put("tools", toToolDefinitions(tools));
        }

        JsonNode calls = post("/api/chat", body).path("message").path("tool_calls");
        if (!calls.isArray() || calls.isEmpty()) {
            log.info("Ollama returned no tool calls for model {}", defaultModel);
            return new String[0];
        }

        List<String> result = new ArrayList<>(calls.size());
        for (JsonNode call : calls) {
            result.add(call.path("function").toString());
        }
        return result.toArray(String[]::new);
    }

    /**
     * Estimate only: Ollama reports exact counts per completion ({@code prompt_eval_count},
     * {@code eval_count}) but exposes no tokenize endpoint, so this uses a character heuristic.
     *
     * @return {@code [estimatedPromptTokens, 0, estimatedPromptTokens]}
     */
    @Override
    public int[] countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return new int[] { 0, 0, 0 };
        }
        int estimate = Math.max(1, (int) Math.ceil(text.length() / (double) CHARS_PER_TOKEN_ESTIMATE));
        return new int[] { estimate, 0, estimate };
    }

    @Override
    public boolean isAvailable() {
        try {
            webClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMillis(AVAILABILITY_PROBE_TIMEOUT_MS));
            return true;
        } catch (RuntimeException ex) {
            log.debug("Ollama availability probe failed: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "ollama";
    }

    @Override
    public String getDefaultModel() {
        return defaultModel;
    }

    private String complete(List<Map<String, Object>> messages) {
        JsonNode response = post("/api/chat", baseBody(messages));

        String content = response.path("message").path("content").asText("");
        if (content.isBlank()) {
            throw new AiProviderException("AI_PROVIDER_ERROR",
                    "Ollama returned an empty message for model " + defaultModel);
        }

        log.info("Ollama completion: model={} promptTokens={} completionTokens={}",
                defaultModel,
                response.path("prompt_eval_count").asInt(0),
                response.path("eval_count").asInt(0));
        return content.strip();
    }

    private Map<String, Object> baseBody(List<Map<String, Object>> messages) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", defaultModel);
        body.put("messages", messages);
        body.put("stream", false);
        body.put("options", Map.of("temperature", temperature, "num_predict", maxTokens));
        return body;
    }

    private JsonNode post(String path, Map<String, Object> body) {
        String raw;
        try {
            raw = webClient.post()
                    .uri(path)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMillis(timeoutMs + BLOCK_GRACE_MS));
        } catch (WebClientResponseException ex) {
            log.error("Ollama rejected the request with {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new AiProviderException("AI_PROVIDER_ERROR",
                    "Ollama rejected the request (" + ex.getStatusCode().value() + ")", ex);
        } catch (IllegalStateException ex) {
            throw new AiProviderException("AI_PROVIDER_TIMEOUT",
                    "Ollama did not respond within " + timeoutMs + " ms", ex);
        } catch (RuntimeException ex) {
            log.error("Ollama request to {} failed", path, ex);
            throw new AiProviderException("AI_PROVIDER_UNAVAILABLE",
                    "Ollama is not reachable at the configured base URL", ex);
        }

        if (raw == null || raw.isBlank()) {
            throw new AiProviderException("AI_PROVIDER_ERROR", "Empty response body from Ollama at " + path);
        }
        try {
            return objectMapper.readTree(raw);
        } catch (IOException ex) {
            throw new AiProviderException("AI_PROVIDER_ERROR", "Ollama returned malformed JSON", ex);
        }
    }

    private static Map<String, Object> message(String role, String content) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content == null ? "" : content);
        return message;
    }

    private static List<Map<String, Object>> toToolDefinitions(Map<String, Object> tools) {
        List<Map<String, Object>> definitions = new ArrayList<>(tools.size());
        tools.forEach((name, definition) -> {
            Map<String, Object> function = new LinkedHashMap<>();
            function.put("name", name);
            if (definition instanceof Map<?, ?> spec) {
                spec.forEach((key, value) -> function.put(String.valueOf(key), value));
            } else {
                function.put("description", String.valueOf(definition));
                function.put("parameters", Map.of("type", "object", "properties", Map.of()));
            }
            definitions.add(Map.of("type", "function", "function", function));
        });
        return definitions;
    }

    /**
     * Keeps the injected context inside the configured window instead of letting it overflow
     * the model and silently truncating the question.
     */
    private String truncateContext(String context) {
        if (context == null) {
            return "";
        }
        int maxChars = maxTokens * CHARS_PER_TOKEN_ESTIMATE;
        if (context.length() <= maxChars) {
            return context;
        }
        log.warn("AI context truncated from {} to {} characters", context.length(), maxChars);
        return context.substring(0, maxChars);
    }
}
