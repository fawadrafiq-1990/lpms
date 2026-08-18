package com.company.legal.hearing.service;

import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.exception.ResourceNotFoundException;
import com.company.legal.hearing.dto.HearingDTO;
import com.company.legal.hearing.entity.Hearing;
import com.company.legal.hearing.repository.HearingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Hearing management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HearingService {

    private final HearingRepository hearingRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Transactional
    public HearingDTO createHearing(HearingDTO dto) {
        log.info("Creating hearing: {}", dto.getHearingNumber());

        Hearing hearing = mapToEntity(dto);
        hearing = hearingRepository.save(hearing);

        return mapToDTO(hearing);
    }

    @Transactional
    public HearingDTO updateHearing(String id, HearingDTO dto) {
        log.info("Updating hearing: {}", id);

        Hearing hearing = hearingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hearing", "id", id));

        hearing.setHearingType(dto.getHearingType());
        hearing.setScheduledDate(dto.getScheduledDate());
        hearing.setScheduledTime(dto.getScheduledTime());
        hearing.setDurationMinutes(dto.getDurationMinutes());
        hearing.setCourtId(dto.getCourtId());
        hearing.setCourtName(dto.getCourtName());
        hearing.setCourtroomId(dto.getCourtroomId());
        hearing.setCourtroomName(dto.getCourtroomName());
        hearing.setJudgeId(dto.getJudgeId());
        hearing.setJudgeName(dto.getJudgeName());
        hearing.setStatus(dto.getStatus());
        hearing.setLocation(dto.getLocation());
        hearing.setVirtualMeetingUrl(dto.getVirtualMeetingUrl());
        hearing.setAgenda(dto.getAgenda());
        hearing.setNotes(dto.getNotes());

        hearing = hearingRepository.save(hearing);

        return mapToDTO(hearing);
    }

    @Transactional(readOnly = true)
    public HearingDTO getHearingById(String id) {
        log.info("Fetching hearing by id: {}", id);

        Hearing hearing = hearingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hearing", "id", id));

        return mapToDTO(hearing);
    }

    @Transactional(readOnly = true)
    public List<HearingDTO> getHearingsByCase(String caseId) {
        log.info("Fetching hearings for case: {}", caseId);

        return hearingRepository.findByCaseId(caseId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<HearingDTO> getHearings(Integer page, Integer size, String status) {
        log.info("Fetching hearings");

        Pageable pageable = PageRequest.of(page != null ? page : 0,
                                          size != null ? size : 20,
                                          Sort.by("scheduledDate").ascending());

        Page<Hearing> hearingPage = hearingRepository.findAll(pageable);

        return PageResponse.from(hearingPage.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public List<HearingDTO> getUpcomingHearings() {
        log.info("Fetching upcoming hearings");

        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        return hearingRepository.findUpcomingHearings(today, nextWeek).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HearingDTO> getTodayAndTomorrowHearings() {
        log.info("Fetching today and tomorrow hearings");

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        return hearingRepository.findTodayAndTomorrowHearings(today, tomorrow).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteHearing(String id) {
        log.info("Deleting hearing: {}", id);

        Hearing hearing = hearingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hearing", "id", id));

        // In production, use soft delete or set status to CANCELLED
        hearingRepository.delete(hearing);
    }

    private Hearing mapToEntity(HearingDTO dto) {
        return Hearing.builder()
                .id(dto.getId())
                .hearingNumber(dto.getHearingNumber() != null ? dto.getHearingNumber() : generateHearingNumber())
                .caseId(dto.getCaseId())
                .caseNumber(dto.getCaseNumber())
                .caseTitle(dto.getCaseTitle())
                .courtId(dto.getCourtId())
                .courtName(dto.getCourtName())
                .courtroomId(dto.getCourtroomId())
                .courtroomName(dto.getCourtroomName())
                .judgeId(dto.getJudgeId())
                .judgeName(dto.getJudgeName())
                .hearingType(dto.getHearingType() != null ? dto.getHearingType() : "OTHER")
                .scheduledDate(dto.getScheduledDate())
                .scheduledTime(dto.getScheduledTime())
                .durationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes() : 60)
                .status(dto.getStatus() != null ? dto.getStatus() : "SCHEDULED")
                .priority(dto.getPriority())
                .location(dto.getLocation())
                .virtualMeetingUrl(dto.getVirtualMeetingUrl())
                .virtualMeetingId(dto.getVirtualMeetingId())
                .agenda(dto.getAgenda())
                .purpose(dto.getPurpose())
                .outcome(dto.getOutcome())
                .notes(dto.getNotes())
                .internalNotes(dto.getInternalNotes())
                .build();
    }

    private HearingDTO mapToDTO(Hearing hearing) {
        return HearingDTO.builder()
                .id(hearing.getId())
                .hearingNumber(hearing.getHearingNumber())
                .caseId(hearing.getCaseId())
                .caseNumber(hearing.getCaseNumber())
                .caseTitle(hearing.getCaseTitle())
                .courtId(hearing.getCourtId())
                .courtName(hearing.getCourtName())
                .courtroomId(hearing.getCourtroomId())
                .courtroomName(hearing.getCourtroomName())
                .judgeId(hearing.getJudgeId())
                .judgeName(hearing.getJudgeName())
                .hearingType(hearing.getHearingType())
                .scheduledDate(hearing.getScheduledDate())
                .scheduledTime(hearing.getScheduledTime())
                .durationMinutes(hearing.getDurationMinutes())
                .actualStartTime(hearing.getActualStartTime())
                .actualEndTime(hearing.getActualEndTime())
                .status(hearing.getStatus())
                .priority(hearing.getPriority())
                .location(hearing.getLocation())
                .virtualMeetingUrl(hearing.getVirtualMeetingUrl())
                .virtualMeetingId(hearing.getVirtualMeetingId())
                .agenda(hearing.getAgenda())
                .purpose(hearing.getPurpose())
                .outcome(hearing.getOutcome())
                .notes(hearing.getNotes())
                .internalNotes(hearing.getInternalNotes())
                .createdAt(hearing.getCreatedAt() != null ? hearing.getCreatedAt().format(DATE_FORMATTER) : null)
                .updatedAt(hearing.getUpdatedAt() != null ? hearing.getUpdatedAt().format(DATE_FORMATTER) : null)
                .createdBy(hearing.getCreatedBy())
                .build();
    }

    private String generateHearingNumber() {
        return "HEAR-" + System.currentTimeMillis();
    }
}