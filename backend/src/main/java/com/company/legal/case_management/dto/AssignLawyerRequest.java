package com.company.legal.case_management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request body for assigning a lawyer to a case.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignLawyerRequest {

    /** lawyers.id (the lawyer profile id). */
    @NotBlank(message = "Lawyer id is required")
    private String lawyerId;

    private String role;

    private Boolean isPrimary;

    private BigDecimal billingRate;

    private Double hoursAllocated;
}
