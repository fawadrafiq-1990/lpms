package com.company.legal.hearing.controller;

import com.company.legal.common.dto.ApiResponse;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.hearing.dto.HearingDTO;
import com.company.legal.hearing.service.HearingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Hearing management.
 */
@RestController
@RequestMapping("/v1/hearings")
@RequiredArgsConstructor
@Tag(name = "Hearings", description = "Hearing management APIs")
public class HearingController {

    private final HearingService hearingService;

    @PostMapping
    @Operation(summary = "Create a new hearing")
    public ResponseEntity<ApiResponse<HearingDTO>> createHearing(@Valid @RequestBody HearingDTO dto) {
        HearingDTO created = hearingService.createHearing(dto);
        return ResponseEntity.ok(ApiResponse.success(created, "Hearing scheduled successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing hearing")
    public ResponseEntity<ApiResponse<HearingDTO>> updateHearing(
            @PathVariable String id,
            @Valid @RequestBody HearingDTO dto) {
        HearingDTO updated = hearingService.updateHearing(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Hearing updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hearing by ID")
    public ResponseEntity<ApiResponse<HearingDTO>> getHearing(@PathVariable String id) {
        HearingDTO hearing = hearingService.getHearingById(id);
        return ResponseEntity.ok(ApiResponse.success(hearing));
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get hearings by case ID")
    public ResponseEntity<ApiResponse<List<HearingDTO>>> getHearingsByCase(@PathVariable String caseId) {
        List<HearingDTO> hearings = hearingService.getHearingsByCase(caseId);
        return ResponseEntity.ok(ApiResponse.success(hearings));
    }

    @GetMapping
    @Operation(summary = "Get all hearings with pagination")
    public ResponseEntity<ApiResponse<PageResponse<HearingDTO>>> getHearings(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String status) {
        PageResponse<HearingDTO> hearings = hearingService.getHearings(page, size, status);
        return ResponseEntity.ok(ApiResponse.success(hearings));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming hearings")
    public ResponseEntity<ApiResponse<List<HearingDTO>>> getUpcomingHearings() {
        List<HearingDTO> hearings = hearingService.getUpcomingHearings();
        return ResponseEntity.ok(ApiResponse.success(hearings));
    }

    @GetMapping("/today")
    @Operation(summary = "Get hearings for today and tomorrow")
    public ResponseEntity<ApiResponse<List<HearingDTO>>> getTodayAndTomorrowHearings() {
        List<HearingDTO> hearings = hearingService.getTodayAndTomorrowHearings();
        return ResponseEntity.ok(ApiResponse.success(hearings));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a hearing")
    public ResponseEntity<ApiResponse<Void>> deleteHearing(@PathVariable String id) {
        hearingService.deleteHearing(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Hearing deleted successfully"));
    }
}