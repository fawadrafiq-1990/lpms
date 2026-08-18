package com.company.legal.common.config;

import com.company.legal.common.filter.RateLimitingFilter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * Rate limiting configuration.
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitConfig {

    private boolean enabled = true;
    private int requestsPerMinute = 60;
    private int burstCapacity = 100;
    private long windowMs = 60000;
    private String redisKeyPrefix = "rate-limit";
    private List<String> whitelistIps = List.of("127.0.0.1", "::1");

    @Bean
    public RateLimitingFilter rateLimitingFilter(StringRedisTemplate redisTemplate) {
        return new RateLimitingFilter(
                redisTemplate,
                enabled,
                requestsPerMinute,
                burstCapacity,
                windowMs,
                redisKeyPrefix,
                whitelistIps
        );
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}