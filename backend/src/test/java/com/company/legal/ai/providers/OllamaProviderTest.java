package com.company.legal.ai.providers;

import com.company.legal.ai.exception.AiProviderException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises {@link OllamaProvider} against a local stub of Ollama's native API,
 * so no Ollama runtime is required. Uses the JDK's built-in HTTP server.
 */
class OllamaProviderTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicReference<String> lastRequestBody = new AtomicReference<>();

    private HttpServer server;
    private String baseUrl;
    private OllamaProvider provider;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        provider = newProvider(60_000, 4096);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    private OllamaProvider newProvider(long timeoutMs, int maxTokens) {
        return new OllamaProvider(baseUrl, "llama3.2", "", 0.7, maxTokens, timeoutMs, mapper);
    }

    private void stub(String path, int status, String body) {
        server.createContext(path, exchange -> {
            lastRequestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
    }

    private JsonNode sentBody() throws IOException {
        return mapper.readTree(lastRequestBody.get());
    }

    @Test
    @DisplayName("chat() calls /api/chat natively and returns the stubbed message content")
    void chatReturnsStubbedContent() throws IOException {
        stub("/api/chat", 200,
                "{\"message\":{\"role\":\"assistant\",\"content\":\"  Seven years.  \"}}");

        String answer = provider.chat("How long is the retention period?");

        assertEquals("Seven years.", answer);
        JsonNode body = sentBody();
        assertEquals("llama3.2", body.path("model").asText());
        assertFalse(body.path("stream").asBoolean(), "Ollama must be called non-streaming");
        assertEquals(1, body.path("messages").size());
        assertEquals("user", body.path("messages").get(0).path("role").asText());
        assertEquals("How long is the retention period?", body.path("messages").get(0).path("content").asText());
        assertEquals(0.7, body.path("options").path("temperature").asDouble(), 1e-9);
        assertEquals(4096, body.path("options").path("num_predict").asInt());
    }

    @Test
    @DisplayName("chatWithContext() prepends a system message carrying the context")
    void chatWithContextSendsSystemMessage() throws IOException {
        stub("/api/chat", 200, "{\"message\":{\"role\":\"assistant\",\"content\":\"ok\"}}");

        String answer = provider.chatWithContext("Summarise", "The client is ACME Ltd.");

        assertEquals("ok", answer);
        JsonNode messages = sentBody().path("messages");
        assertEquals(2, messages.size());
        assertEquals("system", messages.get(0).path("role").asText());
        assertTrue(messages.get(0).path("content").asText().contains("The client is ACME Ltd."),
                "system message must embed the supplied context");
        assertEquals("user", messages.get(1).path("role").asText());
        assertEquals("Summarise", messages.get(1).path("content").asText());
    }

    @Test
    @DisplayName("chatWithTools() sends tool definitions and parses message.tool_calls")
    void chatWithToolsSendsAndParsesToolCalls() throws IOException {
        stub("/api/chat", 200, "{\"message\":{\"role\":\"assistant\",\"content\":\"\","
                + "\"tool_calls\":[{\"function\":{\"name\":\"search_cases\",\"arguments\":{\"query\":\"ACME\"}}}]}}");

        String[] calls = provider.chatWithTools("Find ACME cases",
                Map.of("search_cases", Map.of("description", "Search case files")));

        assertEquals(1, calls.length);
        JsonNode parsed = mapper.readTree(calls[0]);
        assertEquals("search_cases", parsed.path("name").asText());
        assertEquals("ACME", parsed.path("arguments").path("query").asText());

        JsonNode tools = sentBody().path("tools");
        assertEquals(1, tools.size());
        assertEquals("function", tools.get(0).path("type").asText());
        assertEquals("search_cases", tools.get(0).path("function").path("name").asText());
        assertEquals("Search case files", tools.get(0).path("function").path("description").asText());
    }

    @Test
    @DisplayName("context longer than the token window is truncated")
    void longContextIsTruncated() throws IOException {
        OllamaProvider tiny = newProvider(60_000, 10); // 10 tokens * 4 chars = 40 chars max
        stub("/api/chat", 200, "{\"message\":{\"role\":\"assistant\",\"content\":\"ok\"}}");

        tiny.chatWithContext("q", "x".repeat(100));

        String system = sentBody().path("messages").get(0).path("content").asText();
        assertTrue(system.endsWith("x".repeat(40)), "context must be truncated to 40 chars");
    }

    @Test
    @DisplayName("isAvailable() probes /api/tags")
    void isAvailableProbesTags() {
        stub("/api/tags", 200, "{\"models\":[]}");
        assertTrue(provider.isAvailable());
    }

    @Test
    @DisplayName("an unreachable Ollama maps to AI_PROVIDER_UNAVAILABLE")
    void unreachableServerThrowsUnavailable() {
        OllamaProvider dead = new OllamaProvider("http://127.0.0.1:1", "llama3.2", "", 0.7, 4096, 1_000, mapper);

        AiProviderException ex = assertThrows(AiProviderException.class, () -> dead.chat("hello"));
        assertEquals("AI_PROVIDER_UNAVAILABLE", ex.getErrorCode());
        assertFalse(dead.isAvailable(), "availability probe must also report false");
    }

    @Test
    @DisplayName("an HTTP error from Ollama maps to AI_PROVIDER_ERROR")
    void httpErrorMapsToProviderError() {
        stub("/api/chat", 500, "{\"error\":\"boom\"}");

        AiProviderException ex = assertThrows(AiProviderException.class, () -> provider.chat("hello"));
        assertEquals("AI_PROVIDER_ERROR", ex.getErrorCode());
    }

    @Test
    @DisplayName("an empty completion maps to AI_PROVIDER_ERROR")
    void emptyContentMapsToProviderError() {
        stub("/api/chat", 200, "{\"message\":{\"role\":\"assistant\",\"content\":\"\"}}");

        AiProviderException ex = assertThrows(AiProviderException.class, () -> provider.chat("hello"));
        assertEquals("AI_PROVIDER_ERROR", ex.getErrorCode());
    }

    @Test
    @DisplayName("malformed JSON from Ollama maps to AI_PROVIDER_ERROR")
    void malformedJsonMapsToProviderError() {
        stub("/api/chat", 200, "not-json");

        AiProviderException ex = assertThrows(AiProviderException.class, () -> provider.chat("hello"));
        assertEquals("AI_PROVIDER_ERROR", ex.getErrorCode());
    }

    @Test
    @DisplayName("a server slower than the configured timeout maps to AI_PROVIDER_TIMEOUT")
    void slowServerMapsToTimeout() {
        server.createContext("/api/chat", exchange -> {
            try {
                Thread.sleep(6_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            exchange.sendResponseHeaders(200, -1);
        });
        OllamaProvider slow = newProvider(100, 4096); // block bound = 100ms + 5s grace

        AiProviderException ex = assertThrows(AiProviderException.class, () -> slow.chat("hello"));
        assertEquals("AI_PROVIDER_TIMEOUT", ex.getErrorCode());
    }
}
