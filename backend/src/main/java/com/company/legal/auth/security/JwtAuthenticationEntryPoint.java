package com.company.legal.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Authentication Entry Point for handling authentication errors.
 *
 * This class is invoked when authentication is required but:
 * - No credentials were provided
 * - Invalid credentials were provided
 * - Authentication failed
 *
 * Returns a JSON response with appropriate HTTP status codes.
 *
 * @author LPMS Security Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        String errorMessage = "Authentication required. Please provide valid credentials.";
        int statusCode = HttpServletResponse.SC_UNAUTHORIZED;

        // Check for specific error types
        String exceptionMessage = authException.getMessage();

        if (exceptionMessage != null) {
            if (exceptionMessage.contains("Expired") || exceptionMessage.contains("expired")) {
                errorMessage = "Session expired. Please log in again.";
            } else if (exceptionMessage.contains("Invalid") || exceptionMessage.contains("invalid")) {
                errorMessage = "Invalid authentication credentials.";
            } else if (exceptionMessage.contains("Bad credentials")) {
                errorMessage = "Invalid username or password.";
            }
        }

        log.warn("Authentication failed: {} - URI: {}, Message: {}",
                errorMessage,
                request.getRequestURI(),
                authException.getMessage());

        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("WWW-Authenticate", "Bearer realm=\"LPMS API\"");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", Instant.now().toString());
        errorDetails.put("status", statusCode);
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", errorMessage);
        errorDetails.put("path", request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), errorDetails);
    }
}
