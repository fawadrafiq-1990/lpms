package com.company.legal.document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

/**
 * Data Transfer Object for Document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDTO {

    private String id;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    private String caseId;

    private String caseNumber;

    private String caseTitle;

    private String clientId;

    private String clientName;

    private String lawyerId;

    private String lawyerName;

    private String folderId;

    private String category;

    private String documentType;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String mimeType;

    private String checksum;

    private Integer version;

    private Boolean isLatest;

    private Boolean isLocked;

    private String lockedBy;

    private String lockedAt;

    private Boolean isConfidential;

    private String confidentialityLevel;

    private String accessLevel;

    private String tags;

    private String metadata;

    private LocalDate expirationDate;

    private Boolean isExpired;

    private String createdAt;

    private String updatedAt;

    private String createdBy;

    private String createdByName;
}