package com.company.legal.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Time entry entity for tracking billable hours.
 */
@Entity
@Table(name = "time_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntry {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "entry_number", nullable = false, unique = true, length = 50)
    private String entryNumber;

    @Column(name = "case_id", nullable = false, length = 36)
    private String caseId;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "lawyer_id", length = 36)
    private String lawyerId;

    @Column(name = "task_id", length = 36)
    private String taskId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "duration_hours", precision = 6, scale = 2)
    private BigDecimal durationHours;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "activity_type", length = 100)
    private String activityType;

    @Column(name = "billing_rate", precision = 10, scale = 2)
    private BigDecimal billingRate;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Column
    @Builder.Default
    private Boolean billable = true;

    @Column
    @Builder.Default
    private Boolean billed = false;

    @Column(name = "invoice_id", length = 36)
    private String invoiceId;

    @Column(name = "invoice_line_id", length = 36)
    private String invoiceLineId;

    @Column(length = 50)
    @Builder.Default
    private String status = "DRAFT";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;

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
        if (durationHours == null && durationMinutes != null) {
            durationHours = new BigDecimal(durationMinutes).divide(new BigDecimal("60"), 2, java.math.RoundingMode.HALF_UP);
        }
        if (amount == null && billingRate != null && durationHours != null) {
            amount = billingRate.multiply(durationHours);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
            this.durationHours = new BigDecimal(durationMinutes)
                    .divide(new BigDecimal("60"), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    public void calculateAmount() {
        if (billingRate != null && durationHours != null) {
            this.amount = billingRate.multiply(durationHours);
        }
    }

    public void softDelete() {
        this.deletedAt = java.time.LocalDateTime.now();
    }
}