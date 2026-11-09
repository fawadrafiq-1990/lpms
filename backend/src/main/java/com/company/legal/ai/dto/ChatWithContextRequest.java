package com.company.legal.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /v1/ai/chat-with-context}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatWithContextRequest {

    @NotBlank(message = "Message is required")
    private String message;

    /** Optional; when blank the service answers without context. */
    private String context;
}
