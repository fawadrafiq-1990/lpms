package com.company.legal.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Health indicator for Elasticsearch connectivity.
 * Disabled by default; enable with app.health.elasticsearch.enabled=true
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchHealthIndicator implements HealthIndicator {

    private final ElasticsearchOperations elasticsearchOperations;
    private static final boolean enabled = true;

    @Override
    public Health health() {
        if (!enabled) {
            return Health.up()
                    .withDetail("service", "Elasticsearch")
                    .withDetail("status", "disabled")
                    .build();
        }

        try {
            if (elasticsearchOperations == null) {
                return Health.down()
                        .withDetail("reason", "Elasticsearch operations not configured")
                        .build();
            }

            // Simple connectivity check using index existence
            boolean connected = elasticsearchOperations.indexOps(Object.class).exists();

            if (connected) {
                return Health.up()
                        .withDetail("service", "Elasticsearch")
                        .withDetail("status", "connected")
                        .build();
            } else {
                return Health.down()
                        .withDetail("service", "Elasticsearch")
                        .withDetail("status", "index not found")
                        .build();
            }

        } catch (Exception e) {
            log.debug("Elasticsearch health check failed: {}", e.getMessage());
            return Health.down(e)
                    .withDetail("service", "Elasticsearch")
                    .build();
        }
    }
}