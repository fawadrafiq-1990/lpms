package com.company.legal.document.entity;

import com.company.legal.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Document entity for case and client document management.
 */
@Entity
@Table(name = "case_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document extends BaseEntity {

    @Column(name = "document_number", nullable = false, unique = true, length = 50)
    private String documentNumber;

    @Column(name = "case_id", length = 36)
    private String caseId;

    @Column(name = "client_id", length = 36)
    private String clientId;

    @Column(name = "lawyer_id", length = 36)
    private String lawyerId;

    @Column(name = "folder_id", length = 36)
    private String folderId;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "document_type", length = 100)
    private String documentType;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(length = 64)
    private String checksum;

    @Column
    @Builder.Default
    private Integer version = 1;

    @Column(name = "is_latest")
    @Builder.Default
    private Boolean isLatest = true;

    @Column(name = "is_locked")
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "locked_by", length = 36)
    private String lockedBy;

    @Column(name = "locked_at")
    private java.time.LocalDateTime lockedAt;

    @Column(name = "is_confidential")
    @Builder.Default
    private Boolean isConfidential = false;

    @Column(name = "confidentiality_level", length = 50)
    @Builder.Default
    private String confidentialityLevel = "INTERNAL";

    @Column(name = "access_level", length = 50)
    @Builder.Default
    private String accessLevel = "RESTRICTED";

    @Column(columnDefinition = "JSON")
    private String tags;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @Column(name = "expiration_date")
    private java.time.LocalDate expirationDate;

    @Column(name = "is_expired")
    @Builder.Default
    private Boolean isExpired = false;

    public enum DocumentCategory {
        CONTRACT, COURT_ORDER, PLEADING, EVIDENCE, IDENTITY,
        LEGAL_NOTICE, AGREEMENT, EMAIL, REPORT, JUDGMENT, OTHER
    }
}