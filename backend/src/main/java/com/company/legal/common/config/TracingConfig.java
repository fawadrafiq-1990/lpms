package com.company.legal.common.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Distributed tracing configuration using Micrometer Tracing.
 *
 * Provides:
 * - Observation-based manual instrumentation (@Observed)
 * - B3 and W3C trace context propagation
 */
@Configuration
public class TracingConfig {

    /**
     * Enable @Observed annotation support for manual instrumentation.
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }
}