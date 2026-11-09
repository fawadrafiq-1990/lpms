package com.company.legal.ai.controller;

import com.company.legal.ai.exception.AiProviderException;
import com.company.legal.ai.service.AIService;
import com.company.legal.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Standalone MockMvc test. The repo-wide {@code @WebMvcTest} convention cannot load a
 * context right now for reasons unrelated to the AI layer ({@code @EnableJpaAuditing} on
 * the application class and servlet filters that need service beans the slice excludes),
 * so this boots only the controller plus the shared {@link GlobalExceptionHandler}.
 */
class AIControllerTest {

    private final AIService aiService = Mockito.mock(AIService.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AIController(aiService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("chat - returns the service answer with provider and model")
    void chatReturnsAnswer() throws Exception {
        when(aiService.chat("Summarise the case")).thenReturn("A contract dispute.");
        when(aiService.getProvider()).thenReturn("mock");
        when(aiService.getModel()).thenReturn("mock-model");

        mockMvc.perform(post("/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Summarise the case\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("A contract dispute."))
                .andExpect(jsonPath("$.data.provider").value("mock"))
                .andExpect(jsonPath("$.data.model").value("mock-model"));
    }

    @Test
    @DisplayName("chat - a blank message is rejected by bean validation before the service is called")
    void blankMessageRejected() throws Exception {
        mockMvc.perform(post("/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(aiService, never()).chat(anyString());
    }

    @Test
    @DisplayName("chat - a missing message field is rejected")
    void missingMessageRejected() throws Exception {
        mockMvc.perform(post("/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("chat-with-context - passes message and context through")
    void chatWithContextPassesBoth() throws Exception {
        when(aiService.chatWithContext("Summarise", "ACME context")).thenReturn("Summary.");
        when(aiService.getProvider()).thenReturn("mock");

        mockMvc.perform(post("/v1/ai/chat-with-context")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Summarise\",\"context\":\"ACME context\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Summary."))
                .andExpect(jsonPath("$.data.provider").value("mock"));
    }

    @Test
    @DisplayName("chat-with-context - blank message is rejected")
    void chatWithContextBlankMessageRejected() throws Exception {
        mockMvc.perform(post("/v1/ai/chat-with-context")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"\",\"context\":\"ctx\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("info - status is active when the provider is reachable")
    void infoReportsActiveWhenAvailable() throws Exception {
        when(aiService.getProvider()).thenReturn("mock");
        when(aiService.getModel()).thenReturn("mock-model");
        when(aiService.isAvailable()).thenReturn(true);

        mockMvc.perform(get("/v1/ai/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provider").value("mock"))
                .andExpect(jsonPath("$.data.model").value("mock-model"))
                .andExpect(jsonPath("$.data.status").value("active"));
    }

    @Test
    @DisplayName("info - status is unavailable when the provider is not reachable")
    void infoReportsUnavailableWhenDown() throws Exception {
        when(aiService.getProvider()).thenReturn("ollama");
        when(aiService.getModel()).thenReturn("llama3.2");
        when(aiService.isAvailable()).thenReturn(false);

        mockMvc.perform(get("/v1/ai/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("unavailable"));
    }

    @Test
    @DisplayName("an unreachable provider surfaces as 503 with AI_PROVIDER_UNAVAILABLE")
    void unavailableProviderIs503() throws Exception {
        when(aiService.chat(any())).thenThrow(
                new AiProviderException("AI_PROVIDER_UNAVAILABLE", "Ollama is not reachable"));

        mockMvc.perform(post("/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("AI_PROVIDER_UNAVAILABLE"));
    }

    @Test
    @DisplayName("a timed-out provider surfaces as 504 with AI_PROVIDER_TIMEOUT")
    void timedOutProviderIs504() throws Exception {
        when(aiService.chat(any())).thenThrow(
                new AiProviderException("AI_PROVIDER_TIMEOUT", "Ollama did not respond in time"));

        mockMvc.perform(post("/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.errorCode").value("AI_PROVIDER_TIMEOUT"));
    }

    @Test
    @DisplayName("an errored provider surfaces as 502 with AI_PROVIDER_ERROR")
    void erroredProviderIs502() throws Exception {
        when(aiService.chat(any())).thenThrow(
                new AiProviderException("AI_PROVIDER_ERROR", "Ollama returned malformed JSON"));

        mockMvc.perform(post("/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("AI_PROVIDER_ERROR"));
    }
}
