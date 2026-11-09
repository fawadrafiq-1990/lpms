package com.company.legal.ai.controller;

import com.company.legal.ai.dto.ChatRequest;
import com.company.legal.ai.dto.ChatWithContextRequest;
import com.company.legal.ai.service.AIService;
import com.company.legal.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for AI assistant.
 */
@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "AI legal assistant APIs")
public class AIController {

    private final AIService aiService;

    @PostMapping("/chat")
    @Operation(summary = "Send a message to the AI assistant")
    public ResponseEntity<ApiResponse<Map<String, Object>>> chat(@Valid @RequestBody ChatRequest request) {
        String response = aiService.chat(request.getMessage());

        Map<String, Object> data = new HashMap<>();
        data.put("message", response);
        data.put("provider", aiService.getProvider());
        data.put("model", aiService.getModel());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/chat-with-context")
    @Operation(summary = "Send a message with context to the AI assistant")
    public ResponseEntity<ApiResponse<Map<String, Object>>> chatWithContext(
            @Valid @RequestBody ChatWithContextRequest request) {
        String response = aiService.chatWithContext(request.getMessage(), request.getContext());

        Map<String, Object> data = new HashMap<>();
        data.put("message", response);
        data.put("provider", aiService.getProvider());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/info")
    @Operation(summary = "Get AI service information")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("provider", aiService.getProvider());
        data.put("model", aiService.getModel());
        data.put("status", aiService.isAvailable() ? "active" : "unavailable");

        return ResponseEntity.ok(ApiResponse.success(data));
    }

}