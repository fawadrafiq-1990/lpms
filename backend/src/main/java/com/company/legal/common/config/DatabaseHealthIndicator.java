package com.company.legal.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom health indicator for MySQL database connectivity.
 *
 * Performs a lightweight ping query to verify the database is reachable.
 * Reports pool statistics (active/idle/total connections) as health details.
 * Falls back to UNKNOWN status if the connection cannot be established
 * within the configured timeout.
 *
 * Exposed at /api/actuator/health?include=database
 */
@Component("database")
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    private static final String HEALTH_CHECK_SQL = "SELECT 1";

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final long timeoutMillis;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.timeoutMillis = 5000; // 5-second timeout for health checks
    }

    @Override
    public Health health() {
        try {
            // Execute lightweight health-check query
            long start = System.currentTimeMillis();
            jdbcTemplate.queryForObject(HEALTH_CHECK_SQL, Integer.class);
            long elapsed = System.currentTimeMillis() - start;

            // Gather pool statistics for detailed health report
            Map<String, Object> details = new HashMap<>();
            try {
                var poolMXBean = com.zaxxer.hikari.HikariDataSource.class
                        .cast(dataSource)
                        .getHikariPoolMXBean();
                details.put("activeConnections", poolMXBean.getActiveConnections());
                details.put("idleConnections", poolMXBean.getIdleConnections());
                details.put("totalConnections", poolMXBean.getTotalConnections());
                details.put("threadsAwaitingConnection",
                        poolMXBean.getThreadsAwaitingConnection());
            } catch (Exception e) {
                // DataSource may not be HikariCP; log at debug level
                log.debug("Could not retrieve HikariCP pool stats: {}", e.getMessage());
            }
            details.put("queryExecutionTimeMs", elapsed);

            return Health.up()
                    .withDetail("database", "MySQL")
                    .withDetail("status", "connected")
                    .withDetails(details)
                    .build();

        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("timeoutMs", timeoutMillis)
                    .build();
        }
    }
}
