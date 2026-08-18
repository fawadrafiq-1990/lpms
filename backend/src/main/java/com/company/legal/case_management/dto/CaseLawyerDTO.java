package com.company.legal.case_management.dto;

import lombok.*;

/**
 * Data Transfer Object for Case-Lawyer relationship.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseLawyerDTO {

    private String id;

    private String caseId;

    private String lawyerId;

    private String lawyerName;

    private String role;

    private Boolean isPrimary;

    private Double billingRate;

    private Double hoursAllocated;

    private Double hoursLogged;

    private String assignedAt;

    private String assignedBy;
}