package com.company.legal.hearing.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Data Transfer Object for Hearing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HearingDTO {

    private String id;

    private String hearingNumber;

    private String caseId;

    private String caseNumber;

    private String caseTitle;

    private String courtId;

    private String courtName;

    private String courtroomId;

    private String courtroomName;

    private String judgeId;

    private String judgeName;

    private String hearingType;

    private LocalDate scheduledDate;

    private LocalTime scheduledTime;

    private Integer durationMinutes;

    private LocalTime actualStartTime;

    private LocalTime actualEndTime;

    private String status;

    private String priority;

    private String location;

    private String virtualMeetingUrl;

    private String virtualMeetingId;

    private String agenda;

    private String purpose;

    private String outcome;

    private String notes;

    private String internalNotes;

    private String createdAt;

    private String updatedAt;

    private String createdBy;
}