package com.company.legal.document.service;

import com.company.legal.document.dto.DocumentDTO;
import com.company.legal.document.entity.Document;
import com.company.legal.document.repository.DocumentRepository;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.exception.ResourceNotFoundException;
import com.company.legal.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;

/**
 * Service for Document management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Transactional
    public DocumentDTO createDocument(DocumentDTO dto) {
        log.info("Creating document: {}", dto.getTitle());

        Document document = mapToEntity(dto);
        // Metadata-only creation is allowed (no file yet); provide sane
        // non-null defaults for the NOT NULL file columns so the row persists.
        if (document.getFileName() == null) {
            document.setFileName(dto.getDocumentNumber() != null ? dto.getDocumentNumber() : "untitled");
        }
        if (document.getFilePath() == null) {
            document.setFilePath("uploads/" + document.getFileName());
        }
        if (document.getFileSize() == null) {
            document.setFileSize(0L);
        }
        if (document.getMimeType() == null) {
            document.setMimeType("application/octet-stream");
        }
        document = documentRepository.save(document);

        return mapToDTO(document);
    }

    @Transactional
    public DocumentDTO uploadDocument(String caseId, MultipartFile file) {
        log.info("Uploading document for case: {}", caseId);

        try {
            Document document = Document.builder()
                    .documentNumber(generateDocumentNumber())
                    .caseId(caseId)
                    .title(file.getOriginalFilename())
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .category("OTHER")
                    .version(1)
                    .isLatest(true)
                    .isConfidential(false)
                    .confidentialityLevel("INTERNAL")
                    .checksum(calculateChecksum(file.getBytes()))
                    .build();

            document = documentRepository.save(document);

            return mapToDTO(document);
        } catch (Exception e) {
            log.error("Error uploading document", e);
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    @Transactional(readOnly = true)
    public DocumentDTO getDocumentById(String id) {
        log.info("Fetching document by id: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        return mapToDTO(document);
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentDTO> getAllDocuments(Integer page, Integer size) {
        log.info("Fetching all documents (page={}, size={})", page, size);

        Pageable pageable = PageRequest.of(page != null ? page : 0,
                                           size != null ? size : 20,
                                           Sort.by("createdAt").descending());

        Page<Document> documentPage = documentRepository.findAll(pageable);

        return PageResponse.from(documentPage.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentDTO> getDocumentsByCase(String caseId, Integer page, Integer size) {
        log.info("Fetching documents for case: {}", caseId);

        Pageable pageable = PageRequest.of(page != null ? page : 0,
                                          size != null ? size : 20,
                                          Sort.by("createdAt").descending());

        Page<Document> documentPage = documentRepository.findByCaseId(caseId, pageable);

        return PageResponse.from(documentPage.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentDTO> getDocumentsByClient(String clientId, Integer page, Integer size) {
        log.info("Fetching documents for client: {}", clientId);

        Pageable pageable = PageRequest.of(page != null ? page : 0,
                                          size != null ? size : 20,
                                          Sort.by("createdAt").descending());

        Page<Document> documentPage = documentRepository.findByClientId(clientId, pageable);

        return PageResponse.from(documentPage.map(this::mapToDTO));
    }

    @Transactional
    public void deleteDocument(String id) {
        log.info("Deleting document: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        document.softDelete();
        documentRepository.save(document);
    }

    @Transactional
    public DocumentDTO lockDocument(String id) {
        log.info("Locking document: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        document.setIsLocked(true);
        document.setLockedBy(SecurityUtils.getUserId());
        document.setLockedAt(java.time.LocalDateTime.now());

        document = documentRepository.save(document);

        return mapToDTO(document);
    }

    @Transactional
    public DocumentDTO unlockDocument(String id) {
        log.info("Unlocking document: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        document.setIsLocked(false);
        document.setLockedBy(null);
        document.setLockedAt(null);

        document = documentRepository.save(document);

        return mapToDTO(document);
    }

    private String generateDocumentNumber() {
        return "DOC-" + System.currentTimeMillis();
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }

    private Document mapToEntity(DocumentDTO dto) {
        return Document.builder()
                .documentNumber(dto.getDocumentNumber())
                .caseId(dto.getCaseId())
                .clientId(dto.getClientId())
                .lawyerId(dto.getLawyerId())
                .folderId(dto.getFolderId())
                .category(dto.getCategory() != null ? dto.getCategory() : "OTHER")
                .documentType(dto.getDocumentType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .fileName(dto.getFileName())
                .filePath(dto.getFilePath())
                .fileSize(dto.getFileSize())
                .mimeType(dto.getMimeType())
                .checksum(dto.getChecksum())
                .version(dto.getVersion() != null ? dto.getVersion() : 1)
                .isLatest(dto.getIsLatest() != null ? dto.getIsLatest() : true)
                .isLocked(dto.getIsLocked() != null ? dto.getIsLocked() : false)
                .lockedBy(dto.getLockedBy())
                .isConfidential(dto.getIsConfidential() != null ? dto.getIsConfidential() : false)
                .confidentialityLevel(dto.getConfidentialityLevel() != null ? dto.getConfidentialityLevel() : "INTERNAL")
                .accessLevel(dto.getAccessLevel() != null ? dto.getAccessLevel() : "RESTRICTED")
                .tags(dto.getTags())
                .metadata(dto.getMetadata())
                .expirationDate(dto.getExpirationDate())
                .build();
    }

    private DocumentDTO mapToDTO(Document document) {
        return DocumentDTO.builder()
                .id(document.getId())
                .documentNumber(document.getDocumentNumber())
                .caseId(document.getCaseId())
                .clientId(document.getClientId())
                .lawyerId(document.getLawyerId())
                .folderId(document.getFolderId())
                .category(document.getCategory())
                .documentType(document.getDocumentType())
                .title(document.getTitle())
                .description(document.getDescription())
                .fileName(document.getFileName())
                .filePath(document.getFilePath())
                .fileSize(document.getFileSize())
                .mimeType(document.getMimeType())
                .checksum(document.getChecksum())
                .version(document.getVersion())
                .isLatest(document.getIsLatest())
                .isLocked(document.getIsLocked())
                .lockedBy(document.getLockedBy())
                .isConfidential(document.getIsConfidential())
                .confidentialityLevel(document.getConfidentialityLevel())
                .accessLevel(document.getAccessLevel())
                .tags(document.getTags())
                .metadata(document.getMetadata())
                .expirationDate(document.getExpirationDate())
                .isExpired(document.getIsExpired())
                .createdAt(document.getCreatedAt() != null ? document.getCreatedAt().format(DATE_FORMATTER) : null)
                .updatedAt(document.getUpdatedAt() != null ? document.getUpdatedAt().format(DATE_FORMATTER) : null)
                .createdBy(document.getCreatedBy())
                .build();
    }
}