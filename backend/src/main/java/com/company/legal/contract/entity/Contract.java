package com.company.legal.contract.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Client engagement contract entity.
 */
@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "contract_number", nullable = false, unique = true, length = 50)
    private String contractNumber;

    @Column(name = "law_firm_id", nullable = false, length = 36)
    private String lawFirmId;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "client_name", length = 255)
    private String clientName;

    @Column(name = "case_id", length = 36)
    private String caseId;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "engagement_type", nullable = false, length = 50)
    private String engagementType;

    @Column(name = "fee_agreement", nullable = false, length = 50)
    private String feeAgreement;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "retainer_amount", precision = 12, scale = 2)
    private BigDecimal retainerAmount;

    @Column(name = "retainer_balance", precision = 12, scale = 2)
    private BigDecimal retainerBalance;

    @Column(name = "cap_amount", precision = 12, scale = 2)
    private BigDecimal capAmount;

    @Column(name = "contingency_percentage", precision = 5, scale = 2)
    private BigDecimal contingencyPercentage;

    @Column(name = "flat_fee_amount", precision = 12, scale = 2)
    private BigDecimal flatFeeAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "renewal_date")
    private LocalDate renewalDate;

    @Column(name = "scope", columnDefinition = "TEXT")
    private String scope;

    @Column(name = "terms", columnDefinition = "TEXT")
    private String terms;

    @Column(name = "special_conditions", columnDefinition = "TEXT")
    private String specialConditions;

    @Column(name = "signed_by_client")
    private Boolean signedByClient;

    @Column(name = "signed_date")
    private LocalDate signedDate;

    @Column(name = "signed_by_firm")
    private Boolean signedByFirm;

    @Column(name = "firm_signatory_name", length = 255)
    private String firmSignatoryName;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "document_path", length = 500)
    private String documentPath;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void acceptRetainer(BigDecimal amount) {
        if (retainerBalance != null && retainerBalance.compareTo(amount) >= 0) {
            this.retainerBalance = this.retainerBalance.subtract(amount);
        }
    }
}