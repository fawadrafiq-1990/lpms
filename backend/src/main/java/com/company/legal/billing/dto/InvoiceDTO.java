package com.company.legal.billing.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDTO {
    private String id;
    private String invoiceNumber;
    private String lawFirmId;
    private String caseId;
    private String caseNumber;
    private String clientId;
    private String clientName;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private BigDecimal amountPaid;
    private BigDecimal amountOutstanding;
    private String currency;
    private String status;
    private String billingModel;
    private String notes;
    private String termsAndConditions;
    private String paymentInstructions;
    private LocalDateTime issuedAt;
    private LocalDateTime paidAt;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private List<InvoiceItemDTO> items;
}