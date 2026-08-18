package com.company.legal.testutil;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RedisContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * TestContainer configuration for integration tests using Docker containers.
 * Provides MySQL and Redis containers for realistic integration testing.
 */
@TestConfiguration
public class TestContainerConfig {

    private static final String MYSQL_IMAGE = "mysql:8.0";
    private static final String REDIS_IMAGE = "redis:7-alpine";

    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer() {
        MySQLContainer<?> container = new MySQLContainer<>(DockerImageName.parse(MYSQL_IMAGE))
                .withDatabaseName("lpms_test")
                .withUsername("test")
                .withPassword("test123");
        container.start();
        return container;
    }

    @Bean
    @ServiceConnection
    public RedisContainer<?> redisContainer() {
        RedisContainer<?> container = new RedisContainer<>(DockerImageName.parse(REDIS_IMAGE));
        container.start();
        return container;
    }
}
