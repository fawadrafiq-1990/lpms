package com.company.legal.common.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Configures structured logging support via MDC (Mapped Diagnostic Context).
 *
 * Every HTTP request gets a unique correlation ID injected into MDC, making
 * it possible to trace a single request across all log lines. Trace and Span
 * IDs from distributed-tracing headers (W3C TraceContext / OpenTelemetry,
 * or Zipkin B3) are propagated into MDC for cross-service correlation.
 *
 * Usage in service code:
 * <pre>
 *   MdcContext.put("caseId", caseId);
 *   MdcContext.put("userId", userId);
 *   log.info("Processing document upload");  // correlationId appears automatically
 * </pre>
 *
 * The MDC fields are picked up by the JSON log layout (configured in
 * logback-spring.xml) and emitted as structured JSON keys.
 */
@Configuration
public class LoggingConfig {

    private static final Logger log = LoggerFactory.getLogger(LoggingConfig.class);

    private static final String CORRELATION_ID = "correlationId";
    private static final String TRACE_ID = "traceId";
    private static final String SPAN_ID = "spanId";
    private static final String REQUEST_ID = "requestId";
    private static final String USER_AGENT = "userAgent";
    private static final String CLIENT_IP = "clientIp";

    /**
     * Servlet Filter that enriches MDC with request-scoped context.
     *
     * Runs on every HTTP request:
     * 1. Generates a correlation ID if none exists
     * 2. Extracts trace/span IDs from incoming B3 headers
     * 3. Records client IP and User-Agent
     * 4. Measures request duration
     * 5. Cleans up MDC on completion (critical for thread-pooled environments)
     */
    @Bean
    public Filter mdcLoggingFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response,
                                 FilterChain chain)
                    throws IOException, ServletException {

                jakarta.servlet.http.HttpServletRequest httpRequest =
                        (jakarta.servlet.http.HttpServletRequest) request;

                // Generate correlation ID if not already set
                String correlationId = MDC.get(CORRELATION_ID);
                if (correlationId == null) {
                    correlationId = UUID.randomUUID().toString().replace("-", "");
                }
                MDC.put(CORRELATION_ID, correlationId);
                MDC.put(REQUEST_ID,
                        httpRequest.getHeader("X-Request-ID") != null
                                ? httpRequest.getHeader("X-Request-ID")
                                : correlationId);

                // Propagate distributed tracing IDs (Zipkin B3 format)
                String b3TraceId = httpRequest.getHeader("X-B3-TraceId");
                String b3SpanId = httpRequest.getHeader("X-B3-SpanId");
                if (b3TraceId != null) MDC.put(TRACE_ID, b3TraceId);
                if (b3SpanId != null) MDC.put(SPAN_ID, b3SpanId);

                // Client metadata
                MDC.put(CLIENT_IP, getClientIp(httpRequest));
                MDC.put(USER_AGENT,
                        httpRequest.getHeader("User-Agent") != null
                                ? httpRequest.getHeader("User-Agent").substring(0,
                                        Math.min(200, httpRequest.getHeader("User-Agent").length()))
                                : "-");

                long startTime = System.currentTimeMillis();
                try {
                    chain.doFilter(request, response);
                } finally {
                    long duration = System.currentTimeMillis() - startTime;
                    MDC.put("durationMs", String.valueOf(duration));
                    MDC.put("httpMethod", httpRequest.getMethod());
                    MDC.put("httpUrl", httpRequest.getRequestURI());
                    MDC.put("httpStatus",
                            ((jakarta.servlet.http.HttpServletResponse) response)
                                    .getStatus() + "");

                    // Always clean up to prevent memory leaks
                    MDC.clear();
                }
            }
        };
    }

    /**
     * Extract the real client IP, handling proxy headers (X-Forwarded-For,
     * X-Real-IP) that are set when the application sits behind a reverse
     * proxy or load balancer.
     */
    private String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For can contain multiple IPs; take the first
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * Static utility for attaching arbitrary key-value pairs to the current
     * MDC context. Used by service-layer code to enrich logs with domain data.
     *
     * All entries are automatically removed when the request filter cleans up.
     */
    public static final class MdcContext {

        private MdcContext() {}

        public static void put(String key, String value) {
            if (key != null && value != null) {
                MDC.put(key, value);
            }
        }

        public static void put(String key, Number value) {
            if (key != null && value != null) {
                MDC.put(key, String.valueOf(value));
            }
        }

        public static void put(String key, Boolean value) {
            if (key != null && value != null) {
                MDC.put(key, String.valueOf(value));
            }
        }

        public static String get(String key) {
            return MDC.get(key);
        }

        public static Map<String, String> snapshot() {
            Map<String, String> copy = MDC.getCopyOfContextMap();
            return copy != null ? Map.copyOf(copy) : Map.of();
        }

        public static void clear() {
            MDC.clear();
        }
    }
}
