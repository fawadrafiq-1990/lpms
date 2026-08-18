package com.company.legal.billing.service;

import com.company.legal.billing.dto.TimeEntryDTO;
import com.company.legal.billing.entity.TimeEntry;
import com.company.legal.billing.repository.TimeEntryRepository;
import com.company.legal.case_management.entity.Case;
import com.company.legal.case_management.repository.CaseRepository;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.exception.ResourceNotFoundException;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for time entry management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final CaseRepository caseRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Create a new time entry.
     */
    @Transactional
    public TimeEntryDTO createTimeEntry(TimeEntryDTO dto) {
        log.info("Creating time entry for case: {}", dto.getCaseId());

        Case aCase = caseRepository.findById(dto.getCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", "id", dto.getCaseId()));

        String entryNumber = generateEntryNumber();

        TimeEntry entry = mapToEntity(dto, entryNumber, aCase);
        entry.setStatus("DRAFT");
        entry.calculateDuration();
        entry.calculateAmount();

        entry = timeEntryRepository.save(entry);

        return mapToDTO(entry);
    }

    /**
     * Update an existing time entry.
     */
    @Transactional
    public TimeEntryDTO updateTimeEntry(String id, TimeEntryDTO dto) {
        log.info("Updating time entry: {}", id);

        TimeEntry entry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time Entry", "id", id));

        if (Boolean.TRUE.equals(entry.getBilled())) {
            throw new IllegalStateException("Cannot update a billed time entry");
        }

        entry.setDescription(dto.getDescription());
        entry.setWorkDate(dto.getWorkDate());
        entry.setStartTime(dto.getStartTime());
        entry.setEndTime(dto.getEndTime());
        entry.setActivityType(dto.getActivityType());
        entry.setBillingRate(dto.getBillingRate());
        entry.setHourlyRate(dto.getHourlyRate());
        entry.setBillable(dto.getBillable());
        entry.setNotes(dto.getNotes());

        entry.calculateDuration();
        entry.calculateAmount();

        entry = timeEntryRepository.save(entry);

        return mapToDTO(entry);
    }

    /**
     * Get time entry by ID.
     */
    @Transactional(readOnly = true)
    public TimeEntryDTO getTimeEntryById(String id) {
        log.info("Fetching time entry by id: {}", id);

        TimeEntry entry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time Entry", "id", id));

        return mapToDTO(entry);
    }

    /**
     * Get paginated time entries with filters.
     */
    @Transactional(readOnly = true)
    public PageResponse<TimeEntryDTO> getTimeEntries(Integer page, Integer size,
                                                      String userId, String caseId,
                                                      String startDate, String endDate) {
        log.info("Fetching time entries");

        Pageable pageable = PageRequest.of(page != null ? page : 0,
                                           size != null ? size : 20,
                                           Sort.by("createdAt").descending());

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.MIN;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        if (caseId != null && !caseId.isEmpty()) {
            // Filter by case with date range (no native query support, do client-side)
            List<TimeEntryDTO> result = timeEntryRepository.findByCaseId(caseId).stream()
                    .filter(e -> !e.getWorkDate().isBefore(start) && !e.getWorkDate().isAfter(end))
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            return paginate(result, page, size);
        }

        if (userId != null && !userId.isEmpty()) {
            Page<TimeEntry> entries = timeEntryRepository.findByUserId(userId, pageable);
            return PageResponse.from(entries.map(this::mapToDTO));
        }

        Page<TimeEntry> entries = timeEntryRepository.findByDateRange(start, end, pageable);
        return PageResponse.from(entries.map(this::mapToDTO));
    }

    /**
     * Get time entries for a specific case.
     */
    @Transactional(readOnly = true)
    public List<TimeEntryDTO> getTimeEntriesByCase(String caseId) {
        log.info("Fetching time entries for case: {}", caseId);

        return timeEntryRepository.findByCaseId(caseId).stream()
                .sorted((a, b) -> a.getWorkDate().compareTo(b.getWorkDate()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get unbilled billable entries for a user (for invoice generation).
     */
    @Transactional(readOnly = true)
    public List<TimeEntryDTO> getUnbilledBillableEntries(String userId) {
        log.info("Fetching unbilled billable entries for user: {}", userId);

        return timeEntryRepository.findUnbilledBillableEntries(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mark entries as billed (after linking to invoice).
     */
    @Transactional
    public void markAsBilled(List<String> ids) {
        log.info("Marking {} entries as billed", ids.size());
        timeEntryRepository.markAsBilled(ids);
    }

    /**
     * Delete a time entry (soft delete).
     */
    @Transactional
    public void deleteTimeEntry(String id) {
        log.info("Deleting time entry: {}", id);

        TimeEntry entry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time Entry", "id", id));

        entry.softDelete();
        timeEntryRepository.save(entry);
    }

    // --- Private Helpers ---

    private TimeEntry mapToEntity(TimeEntryDTO dto, String entryNumber, Case aCase) {
        return TimeEntry.builder()
                .entryNumber(entryNumber)
                .caseId(dto.getCaseId())
                .caseNumber(aCase != null ? aCase.getCaseNumber() : dto.getCaseNumber())
                .userId(dto.getUserId())
                .lawyerId(dto.getLawyerId())
                .taskId(dto.getTaskId())
                .workDate(dto.getWorkDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .durationMinutes(dto.getDurationMinutes())
                .durationHours(dto.getDurationHours())
                .description(dto.getDescription())
                .activityType(dto.getActivityType())
                .billingRate(dto.getBillingRate())
                .hourlyRate(dto.getHourlyRate())
                .amount(dto.getAmount())
                .billable(dto.getBillable())
                .billed(false)
                .notes(dto.getNotes())
                .build();
    }

    private TimeEntryDTO mapToDTO(TimeEntry entry) {
        String clientName = null;
        try {
            Case aCase = caseRepository.findById(entry.getCaseId()).orElse(null);
            if (aCase != null && aCase.getClientName() != null) {
                clientName = aCase.getClientName();
            }
        } catch (Exception ignored) {
            // Case may have been deleted; skip client name
        }

        return TimeEntryDTO.builder()
                .id(entry.getId())
                .entryNumber(entry.getEntryNumber())
                .caseId(entry.getCaseId())
                .caseNumber(entry.getCaseNumber())
                .userId(entry.getUserId())
                .lawyerId(entry.getLawyerId())
                .taskId(entry.getTaskId())
                .workDate(entry.getWorkDate())
                .startTime(entry.getStartTime())
                .endTime(entry.getEndTime())
                .durationMinutes(entry.getDurationMinutes())
                .durationHours(entry.getDurationHours())
                .description(entry.getDescription())
                .activityType(entry.getActivityType())
                .billingRate(entry.getBillingRate())
                .hourlyRate(entry.getHourlyRate())
                .amount(entry.getAmount())
                .billable(entry.getBillable())
                .billed(entry.getBilled())
                .invoiceId(entry.getInvoiceId())
                .invoiceLineId(entry.getInvoiceLineId())
                .status(entry.getStatus())
                .notes(entry.getNotes())
                .createdAt(entry.getCreatedAt() != null ? entry.getCreatedAt().format(DATE_FORMATTER) : null)
                .updatedAt(entry.getUpdatedAt() != null ? entry.getUpdatedAt().format(DATE_FORMATTER) : null)
                .build();
    }

    private PageResponse<TimeEntryDTO> paginate(List<TimeEntryDTO> all, Integer page, Integer size) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 20;
        int from = Math.min(p * s, all.size());
        int to = Math.min(from + s, all.size());
        List<TimeEntryDTO> content = (from < to) ? all.subList(from, to) : Collections.emptyList();
        return PageResponse.<TimeEntryDTO>builder()
                .content(content)
                .page(p)
                .size(s)
                .totalElements(all.size())
                .totalPages((int) Math.ceil((double) all.size() / s))
                .first(p == 0)
                .last(to >= all.size())
                .empty(all.isEmpty())
                .build();
    }

    private String generateEntryNumber() {
        long count = timeEntryRepository.count();
        return String.format("TE-%05d", count + 1);
    }
}