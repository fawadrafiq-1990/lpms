package com.company.legal.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight lawyer representation for dropdowns / assignment UI.
 * The {@code id} is the {@code lawyers.id} (what case_lawyers references);
 * {@code userId} and {@code name} are denormalized for display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LawyerDTO {

    private String id;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String barNumber;
    private BigDecimal hourlyRate;
    private String availabilityStatus;
    private Integer totalCases;
}
