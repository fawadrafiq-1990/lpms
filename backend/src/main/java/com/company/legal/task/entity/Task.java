package com.company.legal.task.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Task entity for task management.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "task_number", nullable = false, unique = true, length = 50)
    private String taskNumber;

    @Column(name = "case_id", length = 36)
    private String caseId;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "client_id", length = 36)
    private String clientId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "task_type", length = 100)
    private String taskType;

    @Column(length = 50)
    private String priority;

    @Column(length = 50)
    private String status;

    @Column(name = "assigned_to", nullable = false, length = 36)
    private String assignedTo;

    @Column(name = "assigned_to_name", length = 255)
    private String assignedToName;

    @Column(name = "assigned_by", length = 36)
    private String assignedBy;

    @Column(name = "due_date")
    private java.time.LocalDate dueDate;

    @Column(name = "due_time")
    private java.time.LocalTime dueTime;

    @Column(name = "estimated_hours", precision = 6, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "actual_hours", precision = 6, scale = 2)
    private BigDecimal actualHours;

    @Column
    @Builder.Default
    private Integer progress = 0;

    @Column
    @Builder.Default
    private Boolean billable = true;

    @Column(name = "billing_rate", precision = 10, scale = 2)
    private BigDecimal billingRate;

    @Column(name = "billing_amount", precision = 10, scale = 2)
    private BigDecimal billingAmount;

    @Column(name = "parent_task_id", length = 36)
    private String parentTaskId;

    @Column(columnDefinition = "JSON")
    private String dependsOn;

    @Column(columnDefinition = "JSON")
    private String tags;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "completed_at")
    private java.time.LocalDateTime completedAt;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Version
    private Integer version;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = java.time.LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = java.time.LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    public enum TaskPriority {
        LOW, MEDIUM, HIGH, URGENT, CRITICAL
    }

    public enum TaskStatus {
        TODO, IN_PROGRESS, BLOCKED, REVIEW, COMPLETED, CANCELLED
    }
}