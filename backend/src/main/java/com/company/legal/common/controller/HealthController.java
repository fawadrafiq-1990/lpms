package com.company.legal.common.controller;

import com.company.legal.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller.
 */
@RestController
@RequestMapping("/v1")
@Tag(name = "Health", description = "Health check APIs")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Check API health status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "Legal Practice Management System");
        healthData.put("version", "1.0.0");
        healthData.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(healthData, "Service is healthy"));
    }

    @GetMapping("/health/ready")
    @Operation(summary = "Readiness probe")
    public ResponseEntity<ApiResponse<Map<String, Object>>> readiness() {
        Map<String, Object> readinessData = new HashMap<>();
        readinessData.put("ready", true);
        readinessData.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(readinessData));
    }

    @GetMapping("/health/live")
    @Operation(summary = "Liveness probe")
    public ResponseEntity<ApiResponse<Map<String, Object>>> liveness() {
        Map<String, Object> livenessData = new HashMap<>();
        livenessData.put("alive", true);
        livenessData.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(livenessData));
    }
}