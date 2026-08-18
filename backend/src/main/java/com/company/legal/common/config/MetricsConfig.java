package com.company.legal.common.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer metrics configuration for application monitoring.
 */
@Slf4j
@Configuration
public class MetricsConfig {

    @Value("${spring.application.name:lpms-backend}")
    private String applicationName;

    @Value("${law.firm.id:unknown}")
    private String lawFirmId;

    /**
     * Common tags for all metrics - identifies the application and tenant.
     */
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("application", applicationName)
                .commonTags("lawFirm", lawFirmId);
    }

    /**
     * Business metrics: counters for key operations.
     */
    @Bean
    public Counter casesOpenedCounter(MeterRegistry registry) {
        return Counter.builder("business.cases.opened")
                .description("Number of cases opened")
                .register(registry);
    }

    @Bean
    public Counter clientsCreatedCounter(MeterRegistry registry) {
        return Counter.builder("business.clients.created")
                .description("Number of clients created")
                .register(registry);
    }

    @Bean
    public Counter invoicesGeneratedCounter(MeterRegistry registry) {
        return Counter.builder("business.invoices.generated")
                .description("Number of invoices generated")
                .register(registry);
    }

    @Bean
    public Counter paymentsReceivedCounter(MeterRegistry registry) {
        return Counter.builder("business.payments.received")
                .description("Number of payments received")
                .register(registry);
    }

    @Bean
    public Counter conflictChecksCounter(MeterRegistry registry) {
        return Counter.builder("business.conflict.checks")
                .description("Number of conflict checks performed")
                .register(registry);
    }

    @Bean
    public Timer apiRequestTimer(MeterRegistry registry) {
        return Timer.builder("api.requests.duration")
                .description("API request duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    @Bean
    public Timer databaseQueryTimer(MeterRegistry registry) {
        return Timer.builder("database.queries.duration")
                .description("Database query duration")
                .register(registry);
    }
}