package com.company.legal.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lawyer profile entity. One row per user who practises law (linked via
 * {@code user_id}). The {@code id} of this entity is what {@code case_lawyers}
 * and {@code cases.lead_lawyer_id} reference.
 *
 * <p>Note: does not extend {@code BaseEntity} because the {@code lawyers}
 * table only carries {@code id}/{@code created_at}/{@code updated_at}/
 * {@code version} (no {@code created_by}/{@code updated_by}/{@code deleted_at}
 * audit columns).
 */
@Entity
@Table(name = "lawyers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lawyer {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true, length = 36)
    private String userId;

    @Column(name = "law_firm_id", nullable = false, length = 36)
    private String lawFirmId;

    @Column(name = "bar_number", length = 100)
    private String barNumber;

    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Column(name = "license_issue_date")
    private LocalDate licenseIssueDate;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "license_issuing_authority", length = 255)
    private String licenseIssuingAuthority;

    @Column(columnDefinition = "JSON")
    private String specializations;

    @Column(columnDefinition = "JSON")
    private String languages;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "hourly_rate")
    @Builder.Default
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    @Column(name = "availability_status", length = 50)
    @Builder.Default
    private String availabilityStatus = "AVAILABLE";

    @Column(name = "case_load_limit")
    @Builder.Default
    private Integer caseLoadLimit = 50;

    @Column(name = "profile_complete")
    @Builder.Default
    private Boolean profileComplete = false;

    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_cases")
    @Builder.Default
    private Integer totalCases = 0;

    @Column(name = "closed_cases")
    @Builder.Default
    private Integer closedCases = 0;

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
