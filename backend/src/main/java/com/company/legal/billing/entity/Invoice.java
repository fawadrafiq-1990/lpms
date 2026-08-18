package com.company.legal.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Invoice entity for billing management.
 */
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(name = "law_firm_id", nullable = false, length = 36)
    private String lawFirmId;

    @Column(name = "case_id", nullable = false, length = 36)
    private String caseId;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "client_name", length = 255)
    private String clientName;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "billing_period_start")
    private LocalDate billingPeriodStart;

    @Column(name = "billing_period_end")
    private LocalDate billingPeriodEnd;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "amount_paid", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "amount_outstanding", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal amountOutstanding = BigDecimal.ZERO;

    @Column(length = 3)
    @Builder.Default
    private String currency = "AED";

    @Column(length = 50)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "billing_model", length = 50)
    private String billingModel;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "payment_instructions", columnDefinition = "TEXT")
    private String paymentInstructions;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

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
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        calculateOutstanding();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateOutstanding();
    }

    private void calculateOutstanding() {
        this.amountOutstanding = this.total.subtract(this.amountPaid);
        if (this.amountOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            this.amountOutstanding = BigDecimal.ZERO;
        }
    }

    public void updateTotals(BigDecimal subtotal, BigDecimal taxAmount, BigDecimal discountAmount) {
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.discountAmount = discountAmount;
        this.total = subtotal.add(taxAmount).subtract(discountAmount);
        calculateOutstanding();
    }

    /**
     * Calculate totals from linked invoice items.
     */
    public void calculateTotals(List<InvoiceItem> itemDtos) {
        if (itemDtos == null || itemDtos.isEmpty()) {
            this.subtotal = BigDecimal.ZERO;
            this.taxAmount = BigDecimal.ZERO;
            this.discountAmount = BigDecimal.ZERO;
            this.total = BigDecimal.ZERO;
        } else {
            BigDecimal sub = BigDecimal.ZERO;
            BigDecimal tax = BigDecimal.ZERO;
            BigDecimal disc = BigDecimal.ZERO;
            for (var it : itemDtos) {
                if (it.getAmount() != null) sub = sub.add(it.getAmount());
                if (it.getTaxAmount() != null) tax = tax.add(it.getTaxAmount());
                if (it.getDiscountAmount() != null) disc = disc.add(it.getDiscountAmount());
            }
            this.subtotal = sub;
            this.taxAmount = tax;
            this.discountAmount = disc;
            this.total = sub.add(tax).subtract(disc);
        }
        calculateOutstanding();
    }

    public enum InvoiceStatus {
        DRAFT, ISSUED, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED
    }
}