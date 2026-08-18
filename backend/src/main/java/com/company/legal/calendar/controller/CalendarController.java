package com.company.legal.calendar.controller;

import com.company.legal.calendar.dto.CalendarEventDTO;
import com.company.legal.calendar.service.CalendarEventService;
import com.company.legal.common.dto.ApiResponse;
import com.company.legal.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for calendar events.
 */
@RestController
@RequestMapping("/v1/calendar/events")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "Calendar event APIs")
public class CalendarController {

    private final CalendarEventService calendarEventService;

    @PostMapping
    @Operation(summary = "Create a calendar event")
    public ResponseEntity<ApiResponse<CalendarEventDTO>> createEvent(@Valid @RequestBody CalendarEventDTO dto) {
        CalendarEventDTO created = calendarEventService.createEvent(dto);
        return ResponseEntity.ok(ApiResponse.success(created, "Event created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a calendar event")
    public ResponseEntity<ApiResponse<CalendarEventDTO>> updateEvent(
            @PathVariable String id, @Valid @RequestBody CalendarEventDTO dto) {
        CalendarEventDTO updated = calendarEventService.updateEvent(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Event updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an event by ID")
    public ResponseEntity<ApiResponse<CalendarEventDTO>> getEvent(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(calendarEventService.getEvent(id)));
    }

    @GetMapping
    @Operation(summary = "List events (optionally filtered by a date range)")
    public ResponseEntity<ApiResponse<List<CalendarEventDTO>>> getEvents(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        // Default to a wide window (current year) if no range supplied.
        LocalDateTime rangeStart = start != null ? LocalDateTime.parse(start) : LocalDateTime.now().withDayOfYear(1);
        LocalDateTime rangeEnd = end != null ? LocalDateTime.parse(end) : LocalDateTime.now().plusYears(1);
        List<CalendarEventDTO> events = calendarEventService.getEventsInRange(rangeStart, rangeEnd);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/paged")
    @Operation(summary = "List events (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<CalendarEventDTO>>> getEventsPaged(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(ApiResponse.success(calendarEventService.getEvents(page, size)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a calendar event")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable String id) {
        calendarEventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Event deleted successfully"));
    }
}
