package com.company.legal.billing.controller;

import com.company.legal.billing.dto.TimeEntryDTO;
import com.company.legal.billing.service.TimeEntryService;
import com.company.legal.common.dto.ApiResponse;
import com.company.legal.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Time Entry management.
 */
@RestController
@RequestMapping("/v1/time-entries")
@RequiredArgsConstructor
@Tag(name = "Time Entries", description = "Time tracking and billing APIs")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @PostMapping
    @Operation(summary = "Create a new time entry")
    public ResponseEntity<ApiResponse<TimeEntryDTO>> createTimeEntry(@Valid @RequestBody TimeEntryDTO dto) {
        TimeEntryDTO created = timeEntryService.createTimeEntry(dto);
        return ResponseEntity.ok(ApiResponse.success(created, "Time entry created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing time entry")
    public ResponseEntity<ApiResponse<TimeEntryDTO>> updateTimeEntry(
            @PathVariable String id,
            @Valid @RequestBody TimeEntryDTO dto) {
        TimeEntryDTO updated = timeEntryService.updateTimeEntry(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Time entry updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get time entry by ID")
    public ResponseEntity<ApiResponse<TimeEntryDTO>> getTimeEntry(@PathVariable String id) {
        TimeEntryDTO entry = timeEntryService.getTimeEntryById(id);
        return ResponseEntity.ok(ApiResponse.success(entry));
    }

    @GetMapping
    @Operation(summary = "Get time entries with pagination and filters")
    public ResponseEntity<ApiResponse<PageResponse<TimeEntryDTO>>> getTimeEntries(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String caseId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        PageResponse<TimeEntryDTO> entries = timeEntryService.getTimeEntries(
                page, size, userId, caseId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get all time entries for a case")
    public ResponseEntity<ApiResponse<List<TimeEntryDTO>>> getTimeEntriesByCase(@PathVariable String caseId) {
        List<TimeEntryDTO> entries = timeEntryService.getTimeEntriesByCase(caseId);
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    @GetMapping("/unbilled")
    @Operation(summary = "Get unbilled billable time entries for a user (for invoice generation)")
    public ResponseEntity<ApiResponse<List<TimeEntryDTO>>> getUnbilledBillableEntries(
            @RequestParam String userId) {
        List<TimeEntryDTO> entries = timeEntryService.getUnbilledBillableEntries(userId);
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    @PatchMapping("/mark-billed")
    @Operation(summary = "Mark multiple time entries as billed (after invoicing)")
    public ResponseEntity<ApiResponse<Void>> markAsBilled(@RequestBody List<String> ids) {
        timeEntryService.markAsBilled(ids);
        return ResponseEntity.ok(ApiResponse.success(null, "Entries marked as billed"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a time entry (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteTimeEntry(@PathVariable String id) {
        timeEntryService.deleteTimeEntry(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Time entry deleted"));
    }
}
