package com.company.legal.case_management.service;

import com.company.legal.case_management.dto.AssignLawyerRequest;
import com.company.legal.case_management.dto.CaseDTO;
import com.company.legal.case_management.dto.CaseLawyerDTO;
import com.company.legal.case_management.entity.Case;
import com.company.legal.case_management.entity.CaseLawyer;
import com.company.legal.case_management.repository.CaseLawyerRepository;
import com.company.legal.case_management.repository.CaseRepository;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.exception.ResourceNotFoundException;
import com.company.legal.common.security.SecurityUtils;
import com.company.legal.user.entity.Lawyer;
import com.company.legal.user.entity.User;
import com.company.legal.user.repository.LawyerRepository;
import com.company.legal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for Case management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final CaseLawyerRepository caseLawyerRepository;
    private final LawyerRepository lawyerRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Transactional
    public CaseDTO createCase(CaseDTO dto) {
        log.info("Creating case: {}", dto.getCaseNumber());

        if (caseRepository.existsByCaseNumber(dto.getCaseNumber())) {
            throw new IllegalArgumentException("Case number already exists");
        }

        Case caseEntity = mapToEntity(dto);
        caseEntity.setLawFirmId(SecurityUtils.getLawFirmId());
        caseEntity = caseRepository.save(caseEntity);

        return mapToDTO(caseEntity);
    }

    @Transactional
    public CaseDTO updateCase(String id, CaseDTO dto) {
        log.info("Updating case: {}", id);

        Case caseEntity = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case", "id", id));

        caseEntity.setTitle(dto.getTitle());
        caseEntity.setDescription(dto.getDescription());
        caseEntity.setCaseType(dto.getCaseType());
        caseEntity.setCaseStatus(dto.getCaseStatus());
        caseEntity.setCasePriority(dto.getCasePriority());
        caseEntity.setPracticeArea(dto.getPracticeArea());
        caseEntity.setCourtId(dto.getCourtId());
        caseEntity.setJudgeId(dto.getJudgeId());
        caseEntity.setFilingNumber(dto.getFilingNumber());
        caseEntity.setFilingDate(dto.getFilingDate());
        caseEntity.setCaseValue(dto.getCaseValue());
        caseEntity.setExpectedClosingDate(dto.getExpectedClosingDate());
        caseEntity.setRiskLevel(dto.getRiskLevel());
        caseEntity.setLeadLawyerId(dto.getLeadLawyerId());
        caseEntity.setLeadLawyerName(dto.getLeadLawyerName());

        caseEntity = caseRepository.save(caseEntity);

        return mapToDTO(caseEntity);
    }

    @Transactional(readOnly = true)
    public CaseDTO getCaseById(String id) {
        log.info("Fetching case by id: {}", id);

        Case caseEntity = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case", "id", id));

        return mapToDTO(caseEntity);
    }

    @Transactional(readOnly = true)
    public PageResponse<CaseDTO> getCases(String lawFirmId, Integer page, Integer size,
                                          String search, String status, String type) {
        log.info("Fetching cases for firm: {}", lawFirmId);

        Pageable pageable = PageRequest.of(page != null ? page : 0,
                                          size != null ? size : 20,
                                          Sort.by("createdAt").descending());

        Page<Case> casePage;

        if (search != null && !search.isEmpty()) {
            casePage = caseRepository.searchByLawFirm(lawFirmId, search, pageable);
        } else if (status != null && !status.isEmpty()) {
            casePage = caseRepository.findByLawFirmIdAndCaseStatus(
                    lawFirmId, status, pageable);
        } else {
            casePage = caseRepository.findByLawFirmId(lawFirmId, pageable);
        }

        return PageResponse.from(casePage.map(this::mapToDTO));
    }

    @Transactional
    public void deleteCase(String id) {
        log.info("Deleting case: {}", id);

        Case caseEntity = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case", "id", id));

        caseEntity.softDelete();
        caseRepository.save(caseEntity);
    }

    @Transactional(readOnly = true)
    public List<CaseDTO> getCasesByClient(String clientId) {
        log.info("Fetching cases for client: {}", clientId);

        return caseRepository.findByClientId(clientId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CaseDTO> getInProgressCases(String lawFirmId) {
        log.info("Fetching in-progress cases for firm: {}", lawFirmId);

        return caseRepository.findByLawFirmIdAndCaseStatus(
                lawFirmId, "IN_PROGRESS", PageRequest.of(0, 100))
                .getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ==================== Lawyer assignment ====================

    @Transactional(readOnly = true)
    public List<CaseLawyerDTO> getCaseLawyers(String caseId) {
        log.info("Fetching lawyers for case: {}", caseId);
        return caseLawyerRepository.findByCaseId(caseId).stream()
                .map(cl -> mapCaseLawyerToDTO(cl, resolveLawyerName(cl.getLawyerId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public CaseLawyerDTO assignLawyer(String caseId, AssignLawyerRequest request) {
        log.info("Assigning lawyer {} to case {}", request.getLawyerId(), caseId);

        // Verify the case exists
        caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", "id", caseId));

        // Verify the lawyer profile exists
        Lawyer lawyer = lawyerRepository.findById(request.getLawyerId())
                .orElseThrow(() -> new ResourceNotFoundException("Lawyer", "id", request.getLawyerId()));

        // Upsert: if already assigned, update role/primary
        CaseLawyer existing = caseLawyerRepository
                .findByCaseIdAndLawyerId(caseId, request.getLawyerId()).orElse(null);

        CaseLawyer cl;
        if (existing != null) {
            if (request.getRole() != null) existing.setRole(request.getRole());
            if (request.getIsPrimary() != null) existing.setIsPrimary(request.getIsPrimary());
            if (request.getBillingRate() != null) existing.setBillingRate(request.getBillingRate());
            if (request.getHoursAllocated() != null) existing.setHoursAllocated(request.getHoursAllocated());
            cl = existing;
        } else {
            cl = CaseLawyer.builder()
                    .caseId(caseId)
                    .lawyerId(request.getLawyerId())
                    .role(request.getRole() != null ? request.getRole() : "ASSOCIATE")
                    .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                    .billingRate(request.getBillingRate())
                    .hoursAllocated(request.getHoursAllocated())
                    .hoursLogged(0.0)
                    .assignedAt(java.time.LocalDateTime.now())
                    .assignedBy(SecurityUtils.getUserId())
                    .build();
        }
        cl = caseLawyerRepository.save(cl);

        // If marking primary, demote other primaries on this case
        final String savedId = cl.getId();
        if (Boolean.TRUE.equals(cl.getIsPrimary())) {
            caseLawyerRepository.findByCaseId(caseId).stream()
                    .filter(other -> !other.getId().equals(savedId))
                    .forEach(other -> {
                        other.setIsPrimary(false);
                        caseLawyerRepository.save(other);
                    });
        }

        return mapCaseLawyerToDTO(cl, resolveLawyerName(cl.getLawyerId()));
    }

    @Transactional
    public void removeLawyer(String caseId, String lawyerId) {
        log.info("Removing lawyer {} from case {}", lawyerId, caseId);
        caseLawyerRepository.deleteByCaseIdAndLawyerId(caseId, lawyerId);
    }

    /** Resolves a display name for a lawyer profile id (user's full name). */
    private String resolveLawyerName(String lawyerId) {
        if (lawyerId == null) return null;
        return lawyerRepository.findById(lawyerId)
                .map(Lawyer::getUserId)
                .flatMap(userRepository::findById)
                .map(User::getFullName)
                .orElse(null);
    }

    private CaseLawyerDTO mapCaseLawyerToDTO(CaseLawyer cl, String lawyerName) {
        return CaseLawyerDTO.builder()
                .id(cl.getId())
                .caseId(cl.getCaseId())
                .lawyerId(cl.getLawyerId())
                .lawyerName(lawyerName)
                .role(cl.getRole())
                .isPrimary(cl.getIsPrimary())
                .billingRate(cl.getBillingRate() != null ? cl.getBillingRate().doubleValue() : null)
                .hoursAllocated(cl.getHoursAllocated())
                .hoursLogged(cl.getHoursLogged())
                .assignedAt(cl.getAssignedAt() != null ? cl.getAssignedAt().format(DATE_FORMATTER) : null)
                .assignedBy(cl.getAssignedBy())
                .build();
    }

    // Private helper methods
    private Case mapToEntity(CaseDTO dto) {
        return Case.builder()
                .caseNumber(dto.getCaseNumber())
                .matterNumber(dto.getMatterNumber())
                .clientId(dto.getClientId())
                .clientName(dto.getClientName())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .caseType(dto.getCaseType())
                .caseStatus(dto.getCaseStatus() != null ? dto.getCaseStatus() : "NEW")
                .casePriority(dto.getCasePriority() != null ? dto.getCasePriority() : "MEDIUM")
                .practiceArea(dto.getPracticeArea())
                .courtId(dto.getCourtId())
                .courtName(dto.getCourtName())
                .judgeId(dto.getJudgeId())
                .opposingPartyId(dto.getOpposingPartyId())
                .filingNumber(dto.getFilingNumber())
                .filingDate(dto.getFilingDate())
                .caseValue(dto.getCaseValue())
                .estimatedDuration(dto.getEstimatedDuration())
                .confidentialityLevel(dto.getConfidentialityLevel() != null ? dto.getConfidentialityLevel() : "INTERNAL")
                .riskLevel(dto.getRiskLevel() != null ? dto.getRiskLevel() : "LOW")
                .openingDate(dto.getOpeningDate() != null ? dto.getOpeningDate() : java.time.LocalDate.now())
                .expectedClosingDate(dto.getExpectedClosingDate())
                .referralSource(dto.getReferralSource())
                .leadLawyerId(dto.getLeadLawyerId())
                .leadLawyerName(dto.getLeadLawyerName())
                .billingModel(dto.getBillingModel() != null ? dto.getBillingModel() : "HOURLY")
                .retainerAmount(dto.getRetainerAmount())
                .retainerBalance(dto.getRetainerBalance())
                .totalBilled(dto.getTotalBilled() != null ? dto.getTotalBilled() : BigDecimal.ZERO)
                .totalPaid(dto.getTotalPaid() != null ? dto.getTotalPaid() : BigDecimal.ZERO)
                .totalOutstanding(dto.getTotalOutstanding() != null ? dto.getTotalOutstanding() : BigDecimal.ZERO)
                .build();
    }

    private CaseDTO mapToDTO(Case caseEntity) {
        // Populate assigned lawyers (case_lawyers join)
        List<CaseLawyerDTO> assignedLawyers = caseLawyerRepository.findByCaseId(caseEntity.getId())
                .stream()
                .map(cl -> mapCaseLawyerToDTO(cl, resolveLawyerName(cl.getLawyerId())))
                .collect(Collectors.toList());

        return CaseDTO.builder()
                .id(caseEntity.getId())
                .caseNumber(caseEntity.getCaseNumber())
                .matterNumber(caseEntity.getMatterNumber())
                .clientId(caseEntity.getClientId())
                .clientName(caseEntity.getClientName())
                .title(caseEntity.getTitle())
                .description(caseEntity.getDescription())
                .caseType(caseEntity.getCaseType())
                .caseStatus(caseEntity.getCaseStatus())
                .casePriority(caseEntity.getCasePriority())
                .practiceArea(caseEntity.getPracticeArea())
                .courtId(caseEntity.getCourtId())
                .courtName(caseEntity.getCourtName())
                .judgeId(caseEntity.getJudgeId())
                .opposingPartyId(caseEntity.getOpposingPartyId())
                .filingNumber(caseEntity.getFilingNumber())
                .filingDate(caseEntity.getFilingDate())
                .caseValue(caseEntity.getCaseValue())
                .estimatedDuration(caseEntity.getEstimatedDuration())
                .confidentialityLevel(caseEntity.getConfidentialityLevel())
                .riskLevel(caseEntity.getRiskLevel())
                .openingDate(caseEntity.getOpeningDate())
                .expectedClosingDate(caseEntity.getExpectedClosingDate())
                .actualClosingDate(caseEntity.getActualClosingDate())
                .referralSource(caseEntity.getReferralSource())
                .leadLawyerId(caseEntity.getLeadLawyerId())
                .leadLawyerName(caseEntity.getLeadLawyerName())
                .billingModel(caseEntity.getBillingModel())
                .retainerAmount(caseEntity.getRetainerAmount())
                .retainerBalance(caseEntity.getRetainerBalance())
                .totalBilled(caseEntity.getTotalBilled())
                .totalPaid(caseEntity.getTotalPaid())
                .totalOutstanding(caseEntity.getTotalOutstanding())
                .createdAt(caseEntity.getCreatedAt() != null ? caseEntity.getCreatedAt().format(DATE_FORMATTER) : null)
                .updatedAt(caseEntity.getUpdatedAt() != null ? caseEntity.getUpdatedAt().format(DATE_FORMATTER) : null)
                .createdBy(caseEntity.getCreatedBy())
                .assignedLawyers(assignedLawyers)
                .build();
    }
}