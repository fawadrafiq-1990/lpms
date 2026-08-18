package com.company.legal.case_management.controller;

import com.company.legal.case_management.dto.AssignLawyerRequest;
import com.company.legal.case_management.dto.CaseDTO;
import com.company.legal.case_management.dto.CaseLawyerDTO;
import com.company.legal.case_management.service.CaseService;
import com.company.legal.common.dto.ApiResponse;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Case management.
 */
@RestController
@RequestMapping("/v1/cases")
@RequiredArgsConstructor
@Tag(name = "Cases", description = "Case management APIs")
public class CaseController {

    private final CaseService caseService;

    @PostMapping
    @Operation(summary = "Create a new case")
    public ResponseEntity<ApiResponse<CaseDTO>> createCase(@Valid @RequestBody CaseDTO dto) {
        CaseDTO created = caseService.createCase(dto);
        return ResponseEntity.ok(ApiResponse.success(created, "Case created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing case")
    public ResponseEntity<ApiResponse<CaseDTO>> updateCase(
            @PathVariable String id,
            @Valid @RequestBody CaseDTO dto) {
        CaseDTO updated = caseService.updateCase(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Case updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get case by ID")
    public ResponseEntity<ApiResponse<CaseDTO>> getCase(@PathVariable String id) {
        CaseDTO caseDTO = caseService.getCaseById(id);
        return ResponseEntity.ok(ApiResponse.success(caseDTO));
    }

    @GetMapping
    @Operation(summary = "Get all cases with pagination and filters")
    public ResponseEntity<ApiResponse<PageResponse<CaseDTO>>> getCases(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        String lawFirmId = SecurityUtils.getLawFirmId();
        PageResponse<CaseDTO> cases = caseService.getCases(lawFirmId, page, size, search, status, type);
        return ResponseEntity.ok(ApiResponse.success(cases));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get cases by client ID")
    public ResponseEntity<ApiResponse<List<CaseDTO>>> getCasesByClient(@PathVariable String clientId) {
        List<CaseDTO> cases = caseService.getCasesByClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(cases));
    }

    @GetMapping("/in-progress")
    @Operation(summary = "Get in-progress cases")
    public ResponseEntity<ApiResponse<List<CaseDTO>>> getInProgressCases() {
        String lawFirmId = SecurityUtils.getLawFirmId();
        List<CaseDTO> cases = caseService.getInProgressCases(lawFirmId);
        return ResponseEntity.ok(ApiResponse.success(cases));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a case (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteCase(@PathVariable String id) {
        caseService.deleteCase(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Case deleted successfully"));
    }

    // ==================== Lawyer assignment ====================

    @GetMapping("/{caseId}/lawyers")
    @Operation(summary = "Get lawyers assigned to a case")
    public ResponseEntity<ApiResponse<List<CaseLawyerDTO>>> getCaseLawyers(@PathVariable String caseId) {
        List<CaseLawyerDTO> lawyers = caseService.getCaseLawyers(caseId);
        return ResponseEntity.ok(ApiResponse.success(lawyers));
    }

    @PostMapping("/{caseId}/lawyers")
    @Operation(summary = "Assign a lawyer to a case")
    public ResponseEntity<ApiResponse<CaseLawyerDTO>> assignLawyer(
            @PathVariable String caseId,
            @Valid @RequestBody AssignLawyerRequest request) {
        CaseLawyerDTO assigned = caseService.assignLawyer(caseId, request);
        return ResponseEntity.ok(ApiResponse.success(assigned, "Lawyer assigned successfully"));
    }

    @DeleteMapping("/{caseId}/lawyers/{lawyerId}")
    @Operation(summary = "Remove a lawyer from a case")
    public ResponseEntity<ApiResponse<Void>> removeLawyer(
            @PathVariable String caseId,
            @PathVariable String lawyerId) {
        caseService.removeLawyer(caseId, lawyerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Lawyer removed successfully"));
    }
}