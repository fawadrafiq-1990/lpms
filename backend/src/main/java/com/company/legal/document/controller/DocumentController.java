package com.company.legal.document.controller;

import com.company.legal.document.dto.DocumentDTO;
import com.company.legal.document.service.DocumentService;
import com.company.legal.common.dto.ApiResponse;
import com.company.legal.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for Document management.
 */
@RestController
@RequestMapping("/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document management APIs")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @Operation(summary = "Create a new document record")
    public ResponseEntity<ApiResponse<DocumentDTO>> createDocument(
            @Valid @RequestBody DocumentDTO dto) {
        DocumentDTO created = documentService.createDocument(dto);
        return ResponseEntity.ok(ApiResponse.success(created, "Document created successfully"));
    }

    @PostMapping("/{id}/upload")
    @Operation(summary = "Upload a file for a document")
    public ResponseEntity<ApiResponse<DocumentDTO>> uploadDocument(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        DocumentDTO uploaded = documentService.uploadDocument(id, file);
        return ResponseEntity.ok(ApiResponse.success(uploaded, "Document uploaded successfully"));
    }

    @GetMapping
    @Operation(summary = "List all documents (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<DocumentDTO>>> getAllDocuments(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        PageResponse<DocumentDTO> documents = documentService.getAllDocuments(page, size);
        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<ApiResponse<DocumentDTO>> getDocument(@PathVariable String id) {
        DocumentDTO document = documentService.getDocumentById(id);
        return ResponseEntity.ok(ApiResponse.success(document));
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get documents by case ID")
    public ResponseEntity<ApiResponse<PageResponse<DocumentDTO>>> getDocumentsByCase(
            @PathVariable String caseId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        PageResponse<DocumentDTO> documents = documentService.getDocumentsByCase(caseId, page, size);
        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get documents by client ID")
    public ResponseEntity<ApiResponse<PageResponse<DocumentDTO>>> getDocumentsByClient(
            @PathVariable String clientId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        PageResponse<DocumentDTO> documents = documentService.getDocumentsByClient(clientId, page, size);
        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable String id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Document deleted successfully"));
    }

    @PutMapping("/{id}/lock")
    @Operation(summary = "Lock a document")
    public ResponseEntity<ApiResponse<DocumentDTO>> lockDocument(@PathVariable String id) {
        DocumentDTO locked = documentService.lockDocument(id);
        return ResponseEntity.ok(ApiResponse.success(locked, "Document locked successfully"));
    }

    @PutMapping("/{id}/unlock")
    @Operation(summary = "Unlock a document")
    public ResponseEntity<ApiResponse<DocumentDTO>> unlockDocument(@PathVariable String id) {
        DocumentDTO unlocked = documentService.unlockDocument(id);
        return ResponseEntity.ok(ApiResponse.success(unlocked, "Document unlocked successfully"));
    }
}