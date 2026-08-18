package com.company.legal.hearing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Hearing entity for court hearing management.
 */
@Entity
@Table(name = "hearings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hearing {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hearing_number", nullable = false, unique = true, length = 50)
    private String hearingNumber;

    @Column(name = "case_id", nullable = false, length = 36)
    private String caseId;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "case_title", length = 500)
    private String caseTitle;

    @Column(name = "court_id", nullable = false, length = 36)
    private String courtId;

    @Column(name = "court_name", length = 255)
    private String courtName;

    @Column(name = "courtroom_id", length = 36)
    private String courtroomId;

    @Column(name = "courtroom_name", length = 100)
    private String courtroomName;

    @Column(name = "judge_id", length = 36)
    private String judgeId;

    @Column(name = "judge_name", length = 255)
    private String judgeName;

    @Column(name = "hearing_type", nullable = false, length = 50)
    private String hearingType;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;

    @Column(name = "duration_minutes")
    @Builder.Default
    private Integer durationMinutes = 60;

    @Column(name = "actual_start_time")
    private LocalTime actualStartTime;

    @Column(name = "actual_end_time")
    private LocalTime actualEndTime;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "SCHEDULED";

    @Column(length = 50)
    private String priority;

    @Column(length = 500)
    private String location;

    @Column(name = "virtual_meeting_url", length = 500)
    private String virtualMeetingUrl;

    @Column(name = "virtual_meeting_id", length = 255)
    private String virtualMeetingId;

    @Column(columnDefinition = "TEXT")
    private String agenda;

    @Column(columnDefinition = "TEXT")
    private String purpose;

    @Column(columnDefinition = "TEXT")
    private String outcome;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    @Column(columnDefinition = "JSON")
    private String remindersSent;

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

    public enum HearingType {
        INITIAL, ARGUMENT, EVIDENCE, WITNESS, CROSS_EXAMINATION,
        FINAL_ARGUMENT, JUDGMENT, APPEAL, OTHER
    }

    public enum HearingStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, POSTPONED, ADJOURNED
    }
}