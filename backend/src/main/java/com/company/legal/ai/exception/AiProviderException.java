package com.company.legal.ai.exception;

import com.company.legal.common.exception.BaseException;

/**
 * Raised when the configured LLM provider cannot answer a request.
 *
 * <p>Error codes: {@code AI_PROVIDER_TIMEOUT}, {@code AI_PROVIDER_UNAVAILABLE},
 * {@code AI_PROVIDER_ERROR}.
 */
public class AiProviderException extends BaseException {

    public AiProviderException(String errorCode, String message) {
        super(errorCode, message);
    }

    public AiProviderException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
