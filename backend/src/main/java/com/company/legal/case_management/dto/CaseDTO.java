package com.company.legal.case_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for Case.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseDTO {

    private String id;

    @NotBlank(message = "Case number is required")
    private String caseNumber;

    private String matterNumber;

    @NotNull(message = "Client is required")
    private String clientId;

    private String clientName;

    @NotBlank(message = "Title is required")
    @Size(max = 500)
    private String title;

    private String description;

    @NotBlank(message = "Case type is required")
    private String caseType;

    private String caseStatus;

    private String casePriority;

    private String practiceArea;

    private String courtId;

    private String courtName;

    private String judgeId;

    private String judgeName;

    private String opposingPartyId;

    private String filingNumber;

    private LocalDate filingDate;

    private BigDecimal caseValue;

    private String estimatedDuration;

    private String confidentialityLevel;

    private String riskLevel;

    private LocalDate openingDate;

    private LocalDate expectedClosingDate;

    private LocalDate actualClosingDate;

    private String referralSource;

    private String leadLawyerId;

    private String leadLawyerName;

    private String billingModel;

    private BigDecimal retainerAmount;

    private BigDecimal retainerBalance;

    private BigDecimal totalBilled;

    private BigDecimal totalPaid;

    private BigDecimal totalOutstanding;

    private List<CaseLawyerDTO> assignedLawyers;

    private List<String> tags;

    private String createdAt;

    private String updatedAt;

    private String createdBy;
}