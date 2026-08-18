package com.company.legal.conflict.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Conflict check record tracking potential conflicts between clients/cases.
 */
@Entity
@Table(name = "conflict_checks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConflictCheck {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "law_firm_id", nullable = false, length = 36)
    private String lawFirmId;

    @Column(name = "checked_by_user_id", nullable = false, length = 36)
    private String checkedByUserId;

    @Column(name = "checked_by_name", length = 255)
    private String checkedByName;

    @Column(name = "query_client_id", length = 36)
    private String queryClientId;

    @Column(name = "query_client_name", length = 255)
    private String queryClientName;

    @Column(name = "query_case_id", length = 36)
    private String queryCaseId;

    @Column(name = "related_client_id", length = 36)
    private String relatedClientId;

    @Column(name = "related_client_name", length = 255)
    private String relatedClientName;

    @Column(name = "related_case_id", length = 36)
    private String relatedCaseId;

    @Column(name = "conflict_type", length = 50)
    private String conflictType;

    @Column(name = "match_field", length = 100)
    private String matchField;

    @Column(name = "match_value", length = 255)
    private String matchValue;

    @Column(name = "severity", length = 20)
    private String severity;

    @Column(name = "is_conflict", nullable = false)
    @Builder.Default
    private Boolean isConflict = false;

    @Column(name = "cleared", nullable = false)
    @Builder.Default
    private Boolean cleared = false;

    @Column(name = "cleared_by_user_id", length = 36)
    private String clearedByUserId;

    @Column(name = "cleared_reason", columnDefinition = "TEXT")
    private String clearedReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    public enum ConflictType {
        CLIENT, PARTY, OPPONENT, LAWYER, COURT
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}