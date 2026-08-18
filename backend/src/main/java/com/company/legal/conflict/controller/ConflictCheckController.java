package com.company.legal.conflict.controller;

import com.company.legal.conflict.entity.ConflictCheck;
import com.company.legal.conflict.service.ConflictOfInterestService;
import com.company.legal.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for conflict of interest checks.
 */
@RestController
@RequestMapping("/v1/conflict-checks")
@RequiredArgsConstructor
@Tag(name = "Conflict Checks", description = "Conflict of interest checking APIs")
public class ConflictCheckController {

    private final ConflictOfInterestService conflictService;

    @PostMapping("/check")
    @Operation(summary = "Run conflict of interest check for a client/case")
    public ResponseEntity<ApiResponse<List<ConflictCheck>>> checkConflicts(
            @RequestParam String lawFirmId,
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String queryCaseId,
            @RequestParam(required = false) String queryClientName) {
        List<ConflictCheck> conflicts = conflictService.checkConflicts(lawFirmId, clientId, queryCaseId, queryClientName);
        return ResponseEntity.ok(ApiResponse.success(conflicts,
                conflicts.isEmpty() ? "No conflicts found" : conflicts.size() + " conflict(s) found"));
    }

    @PostMapping
    @Operation(summary = "Save a conflict check result")
    public ResponseEntity<ApiResponse<ConflictCheck>> saveCheck(@RequestBody ConflictCheck check) {
        ConflictCheck saved = conflictService.saveCheck(check);
        return ResponseEntity.ok(ApiResponse.success(saved, "Conflict check saved"));
    }

    @PatchMapping("/{id}/clear")
    @Operation(summary = "Clear a conflict with justification")
    public ResponseEntity<ApiResponse<ConflictCheck>> clearConflict(
            @PathVariable String id,
            @RequestParam String clearedByUserId,
            @RequestParam String reason) {
        ConflictCheck cleared = conflictService.clearConflict(id, clearedByUserId, reason);
        return ResponseEntity.ok(ApiResponse.success(cleared, "Conflict cleared"));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active (uncleared) conflicts for a firm")
    public ResponseEntity<ApiResponse<List<ConflictCheck>>> getActiveConflicts(@RequestParam String lawFirmId) {
        return ResponseEntity.ok(ApiResponse.success(conflictService.getActiveConflicts(lawFirmId)));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get all conflict checks for a client")
    public ResponseEntity<ApiResponse<List<ConflictCheck>>> getClientChecks(
            @RequestParam String lawFirmId,
            @PathVariable String clientId) {
        return ResponseEntity.ok(ApiResponse.success(conflictService.getChecksForClient(lawFirmId, clientId)));
    }
}