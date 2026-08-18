package com.company.legal.calendar.entity;

import com.company.legal.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Calendar event entity. Maps the {@code calendar_events} table. Extends
 * {@link BaseEntity} because the table carries the full audit column set
 * (created_by / updated_by / version).
 */
@Entity
@Table(name = "calendar_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEvent extends BaseEntity {

    @Column(name = "event_number", nullable = false, unique = true, length = 50)
    private String eventNumber;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "case_id", length = 36)
    private String caseId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "all_day")
    private Boolean allDay;

    @Column(name = "location", length = 500)
    private String location;

    @Column(name = "is_virtual")
    private Boolean isVirtual;

    @Column(name = "virtual_meeting_url", length = 500)
    private String virtualMeetingUrl;

    @Column(name = "attendees", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String attendees;

    @Column(name = "visibility", length = 50)
    private String visibility;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "recurrence_rule", length = 500)
    private String recurrenceRule;

    @Column(name = "linked_entity_type", length = 50)
    private String linkedEntityType;

    @Column(name = "linked_entity_id", length = 36)
    private String linkedEntityId;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
