package com.company.legal.calendar.service;

import com.company.legal.calendar.dto.CalendarEventDTO;
import com.company.legal.calendar.entity.CalendarEvent;
import com.company.legal.calendar.repository.CalendarEventRepository;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.exception.ResourceNotFoundException;
import com.company.legal.common.security.SecurityUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for calendar event management. Events are scoped to the current user.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Transactional
    public CalendarEventDTO createEvent(CalendarEventDTO dto) {
        log.info("Creating calendar event: {}", dto.getTitle());

        String userId = SecurityUtils.getUserId();
        CalendarEvent event = mapToEntity(dto);
        event.setUserId(userId);
        if (event.getEventNumber() == null || event.getEventNumber().isBlank()) {
            event.setEventNumber("EVT-" + System.currentTimeMillis());
        }
        if (event.getAllDay() == null) event.setAllDay(false);
        if (event.getStatus() == null) event.setStatus("CONFIRMED");
        if (event.getVisibility() == null) event.setVisibility("PRIVATE");

        event = calendarEventRepository.save(event);
        return mapToDTO(event);
    }

    @Transactional
    public CalendarEventDTO updateEvent(String id, CalendarEventDTO dto) {
        log.info("Updating calendar event: {}", id);
        CalendarEvent event = calendarEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", "id", id));

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getStartDate() != null) event.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) event.setEndDate(dto.getEndDate());
        if (dto.getAllDay() != null) event.setAllDay(dto.getAllDay());
        if (dto.getEventType() != null) event.setEventType(dto.getEventType());
        if (dto.getCaseId() != null) event.setCaseId(dto.getCaseId());
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        if (dto.getColor() != null) event.setColor(dto.getColor());
        if (dto.getNotes() != null) event.setNotes(dto.getNotes());
        if (dto.getAttendees() != null) event.setAttendees(toJson(dto.getAttendees()));

        event = calendarEventRepository.save(event);
        return mapToDTO(event);
    }

    @Transactional(readOnly = true)
    public CalendarEventDTO getEvent(String id) {
        CalendarEvent event = calendarEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", "id", id));
        return mapToDTO(event);
    }

    /**
     * Return events for the current user overlapping the given [start, end] range.
     */
    @Transactional(readOnly = true)
    public List<CalendarEventDTO> getEventsInRange(LocalDateTime start, LocalDateTime end) {
        String userId = SecurityUtils.getUserId();
        List<CalendarEvent> events =
                calendarEventRepository.findForUserInRange(userId, start, end);
        return events.stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<CalendarEventDTO> getEvents(Integer page, Integer size) {
        String userId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page != null ? page : 0,
                size != null ? size : 50,
                Sort.by("startDate").ascending());
        Page<CalendarEvent> eventPage = calendarEventRepository.findByUserId(userId, pageable);
        return PageResponse.from(eventPage.map(this::mapToDTO));
    }

    @Transactional
    public void deleteEvent(String id) {
        CalendarEvent event = calendarEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", "id", id));
        calendarEventRepository.delete(event);
    }

    // ==================== Mapping ====================

    private CalendarEvent mapToEntity(CalendarEventDTO dto) {
        return CalendarEvent.builder()
                .eventNumber(dto.getEventNumber())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .allDay(dto.getAllDay())
                .eventType(dto.getEventType())
                .caseId(dto.getCaseId())
                .location(dto.getLocation())
                .isVirtual(dto.getIsVirtual())
                .virtualMeetingUrl(dto.getVirtualMeetingUrl())
                .attendees(toJson(dto.getAttendees()))
                .visibility(dto.getVisibility())
                .status(dto.getStatus())
                .recurrenceRule(dto.getRecurrenceRule())
                .color(dto.getColor())
                .notes(dto.getNotes())
                .build();
    }

    private CalendarEventDTO mapToDTO(CalendarEvent event) {
        return CalendarEventDTO.builder()
                .id(event.getId())
                .eventNumber(event.getEventNumber())
                .userId(event.getUserId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .allDay(event.getAllDay())
                .eventType(event.getEventType())
                .caseId(event.getCaseId())
                .location(event.getLocation())
                .isVirtual(event.getIsVirtual())
                .virtualMeetingUrl(event.getVirtualMeetingUrl())
                .attendees(fromJson(event.getAttendees()))
                .visibility(event.getVisibility())
                .status(event.getStatus())
                .recurrenceRule(event.getRecurrenceRule())
                .color(event.getColor())
                .notes(event.getNotes())
                .createdAt(event.getCreatedAt() != null ? event.getCreatedAt().format(DATE_FORMATTER) : null)
                .updatedAt(event.getUpdatedAt() != null ? event.getUpdatedAt().format(DATE_FORMATTER) : null)
                .createdBy(event.getCreatedBy())
                .build();
    }

    private String toJson(List<String> attendees) {
        if (attendees == null || attendees.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(attendees);
        } catch (Exception e) {
            log.warn("Failed to serialize attendees", e);
            return null;
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to deserialize attendees: {}", json, e);
            return new ArrayList<>();
        }
    }
}
