package com.company.legal.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Health indicator for Redis connectivity.
 */
@Slf4j
@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final StringRedisTemplate redisTemplate;

    public RedisHealthIndicator(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            long start = System.currentTimeMillis();

            // Simple PING command using connection
            String response = redisTemplate.executePipelined(
                    (org.springframework.data.redis.core.RedisCallback<String>) connection -> {
                        connection.ping();
                        return "PONG";
                    }
            ).toString();

            long elapsed = System.currentTimeMillis() - start;

            // Build health details
            Map<String, Object> details = new HashMap<>();
            details.put("responseTimeMs", elapsed);
            details.put("status", "connected");

            return Health.up()
                    .withDetail("service", "Redis")
                    .withDetails(details)
                    .build();

        } catch (Exception e) {
            log.debug("Redis health check failed: {}", e.getMessage());
            return Health.down(e)
                    .withDetail("service", "Redis")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}