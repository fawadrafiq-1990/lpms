package com.company.legal.common.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Health indicator for Apache Kafka connectivity.
 * Disabled by default; enable with app.health.kafka.enabled=true
 */
@Slf4j
@Component("kafka")
@ConditionalOnProperty(name = "app.health.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaHealthIndicator implements HealthIndicator {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Override
    public Health health() {
        Map<String, Object> adminConfigs = new HashMap<>();
        adminConfigs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        adminConfigs.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);

        try (AdminClient adminClient = AdminClient.create(adminConfigs)) {
            long start = System.currentTimeMillis();

            // Simple connectivity check - describe cluster
            var clusterInfo = adminClient.describeCluster();
            String clusterId = clusterInfo.clusterId().get(5, TimeUnit.SECONDS);
            int brokerCount = clusterInfo.nodes().get(5, TimeUnit.SECONDS).size();

            long elapsed = System.currentTimeMillis() - start;

            return Health.up()
                    .withDetail("service", "Kafka")
                    .withDetail("clusterId", clusterId)
                    .withDetail("brokerCount", brokerCount)
                    .withDetail("responseTimeMs", elapsed)
                    .build();

        } catch (TimeoutException e) {
            return Health.down(e)
                    .withDetail("service", "Kafka")
                    .withDetail("status", "timeout")
                    .build();
        } catch (InterruptedException | ExecutionException e) {
            return Health.down(e)
                    .withDetail("service", "Kafka")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}