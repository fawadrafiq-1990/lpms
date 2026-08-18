package com.company.legal.case_management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Join entity linking a case to an assigned lawyer (maps the
 * {@code case_lawyers} table from V3 migration).
 *
 * <p>{@code lawyerId} references {@code lawyers.id} (the Lawyer profile id),
 * not the user id.
 *
 * <p>Note: does not extend {@code BaseEntity} because the {@code case_lawyers}
 * table only carries {@code id}/{@code created_at}/{@code updated_at}/
 * {@code version} (no audit {@code created_by}/{@code updated_by}/
 * {@code deleted_at} columns).
 */
@Entity
@Table(name = "case_lawyers",
        uniqueConstraints = @UniqueConstraint(name = "uk_case_lawyers_case_lawyer",
                columnNames = {"case_id", "lawyer_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseLawyer {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "case_id", nullable = false, length = 36)
    private String caseId;

    /** References lawyers.id. */
    @Column(name = "lawyer_id", nullable = false, length = 36)
    private String lawyerId;

    @Column(length = 50)
    @Builder.Default
    private String role = "ASSOCIATE";

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "billing_rate")
    private BigDecimal billingRate;

    @Column(name = "hours_allocated")
    private Double hoursAllocated;

    @Column(name = "hours_logged")
    @Builder.Default
    private Double hoursLogged = 0.0;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by", length = 36)
    private String assignedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    @Builder.Default
    private Integer version = 0;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
