package com.company.legal.billing.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for time entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntryDTO {
    private String id;
    private String entryNumber;
    private String caseId;
    private String caseNumber;
    private String userId;
    private String userName;
    private String lawyerId;
    private String taskId;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;
    private BigDecimal durationHours;
    private String description;
    private String activityType;
    private BigDecimal billingRate;
    private BigDecimal hourlyRate;
    private BigDecimal amount;
    private Boolean billable;
    private Boolean billed;
    private String invoiceId;
    private String invoiceLineId;
    private String status;
    private String notes;
    private String createdAt;
    private String updatedAt;
}
