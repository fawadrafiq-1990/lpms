package com.company.legal.workflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Workflow definition entity.
 */
@Entity
@Table(name = "workflow_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDefinition {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "workflow_type", nullable = false, length = 100)
    private String workflowType;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    @Builder.Default
    private Integer version = 1;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(columnDefinition = "JSON")
    private String steps;

    @Column(columnDefinition = "JSON")
    private String triggers;

    @Column(columnDefinition = "JSON")
    private String settings;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Version
    private Integer versionLock;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = java.time.LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}