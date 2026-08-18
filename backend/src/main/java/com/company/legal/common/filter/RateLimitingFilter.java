package com.company.legal.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Rate limiting filter using Redis-backed token bucket algorithm.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final boolean rateLimitEnabled;
    private final int requestsPerMinute;
    private final int burstCapacity;
    private final long windowMs;
    private final String redisKeyPrefix;
    private final List<String> whitelistIps;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);

        // Skip whitelisted IPs
        if (whitelistIps != null && whitelistIps.contains(clientIp)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = redisKeyPrefix + ":" + clientIp;
        long now = System.currentTimeMillis();

        // Simple sliding window rate limiting
        Long requestCount = redisTemplate.opsForValue().increment(key);
        if (requestCount == 1) {
            redisTemplate.expire(key, windowMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        if (requestCount != null && requestCount > requestsPerMinute) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset",
                    String.valueOf(now + windowMs));

            Map<String, Object> error = Map.of(
                    "error", "Rate limit exceeded",
                    "message", "Too many requests. Please try again later.",
                    "retryAfter", windowMs / 1000
            );
            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        // Add rate limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.setHeader("X-RateLimit-Remaining",
                String.valueOf(Math.max(0, requestsPerMinute - (requestCount != null ? requestCount : 0))));
        response.setHeader("X-RateLimit-Reset", String.valueOf(now + windowMs));

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || ip.contains(",")) {
            ip = request.getHeader("X-Real-IP");
        }
        if (!StringUtils.hasText(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}