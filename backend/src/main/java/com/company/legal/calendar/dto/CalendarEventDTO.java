package com.company.legal.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for calendar events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEventDTO {

    private String id;
    private String eventNumber;

    private String userId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Boolean allDay;

    @NotBlank(message = "Event type is required")
    private String eventType;

    private String caseId;
    private String location;
    private Boolean isVirtual;
    private String virtualMeetingUrl;
    private List<String> attendees;
    private String visibility;
    private String status;
    private String recurrenceRule;
    private String color;
    private String notes;

    private String createdAt;
    private String updatedAt;
    private String createdBy;
}
