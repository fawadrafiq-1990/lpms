package com.company.legal.case_management.entity;

import com.company.legal.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Case entity representing a legal matter.
 */
@Entity
@Table(name = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Case extends BaseEntity {

    @Column(name = "case_number", nullable = false, unique = true, length = 50)
    private String caseNumber;

    @Column(name = "matter_number", length = 50)
    private String matterNumber;

    @Column(name = "law_firm_id", nullable = false, length = 36)
    private String lawFirmId;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "case_type", nullable = false, length = 50)
    private String caseType;

    @Column(name = "case_status", nullable = false, length = 50)
    @Builder.Default
    private String caseStatus = "NEW";

    @Column(name = "case_priority", length = 50)
    @Builder.Default
    private String casePriority = "MEDIUM";

    @Column(name = "practice_area", length = 100)
    private String practiceArea;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "court_id", length = 36)
    private String courtId;

    @Column(name = "judge_id", length = 36)
    private String judgeId;

    @Column(name = "opposing_party_id", length = 36)
    private String opposingPartyId;

    @Column(name = "filing_number", length = 100)
    private String filingNumber;

    @Column(name = "filing_date")
    private LocalDate filingDate;

    @Column(name = "case_value", precision = 15, scale = 2)
    private BigDecimal caseValue;

    @Column(name = "estimated_duration", length = 100)
    private String estimatedDuration;

    @Column(name = "confidentiality_level", length = 50)
    @Builder.Default
    private String confidentialityLevel = "INTERNAL";

    @Column(name = "risk_level", length = 50)
    @Builder.Default
    private String riskLevel = "LOW";

    @Column(name = "opening_date")
    private LocalDate openingDate;

    @Column(name = "expected_closing_date")
    private LocalDate expectedClosingDate;

    @Column(name = "actual_closing_date")
    private LocalDate actualClosingDate;

    @Column(name = "referral_source", length = 255)
    private String referralSource;

    @Column(name = "lead_lawyer_id", length = 36)
    private String leadLawyerId;

    @Column(name = "lead_lawyer_name", length = 255)
    private String leadLawyerName;

    @Column(name = "billing_model", length = 50)
    @Builder.Default
    private String billingModel = "HOURLY";

    @Column(name = "retainer_amount", precision = 12, scale = 2)
    private BigDecimal retainerAmount;

    @Column(name = "retainer_balance", precision = 12, scale = 2)
    private BigDecimal retainerBalance;

    @Column(name = "total_billed", precision = 15, scale = 2)
    private BigDecimal totalBilled;

    @Column(name = "total_paid", precision = 15, scale = 2)
    private BigDecimal totalPaid;

    @Column(name = "total_outstanding", precision = 15, scale = 2)
    private BigDecimal totalOutstanding;

    @Column(columnDefinition = "JSON")
    private String settings;

    @Column(name = "client_name", length = 255)
    private String clientName;

    @Column(name = "court_name", length = 255)
    private String courtName;

    public enum CaseStatus {
        NEW, CONFLICT_CHECK, OPEN, PREPARATION, FILED, IN_PROGRESS,
        HEARING, JUDGMENT, APPEAL, SETTLEMENT, CLOSED, ARCHIVED
    }

    public enum CasePriority {
        LOW, MEDIUM, HIGH, URGENT, CRITICAL
    }
}