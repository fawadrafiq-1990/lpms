package com.company.legal.conflict.service;

import com.company.legal.client.entity.Client;
import com.company.legal.client.repository.ClientRepository;
import com.company.legal.case_management.entity.Case;
import com.company.legal.case_management.repository.CaseRepository;
import com.company.legal.conflict.entity.ConflictCheck;
import com.company.legal.conflict.repository.ConflictCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Conflict of interest checker.
 * <p>
 * Runs checks against existing clients, case parties, and opposing parties
 * before a new matter is opened or a client is onboarded.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConflictOfInterestService {

    private final ConflictCheckRepository conflictCheckRepository;
    private final ClientRepository clientRepository;
    private final CaseRepository caseRepository;

    /**
     * Run a full conflict check for a prospective client.
     * Returns a list of potential conflicts found.
     */
    @Transactional(readOnly = true)
    public List<ConflictCheck> checkConflicts(String lawFirmId, String clientId,
                                               String queryCaseId, String queryClientName) {
        log.info("Running conflict check for lawFirm={}, clientId={}", lawFirmId, clientId);
        List<ConflictCheck> conflicts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Load all non-deleted clients for this firm once
        List<Client> allClients = clientRepository.findAll().stream()
                .filter(c -> c.getLawFirmId().equals(lawFirmId) && c.getDeletedAt() == null)
                .toList();

        // 1. Check by exact client name match
        if (queryClientName != null && !queryClientName.isBlank()) {
            String qn = normalize(queryClientName);
            for (Client existing : allClients) {
                if (!existing.getId().equals(clientId)) {
                    String en = normalize(existing.getFullName());
                    if (en.equals(qn) || en.contains(qn) || qn.contains(en)) {
                        ConflictCheck cc = ConflictCheck.builder()
                                .lawFirmId(lawFirmId)
                                .queryClientName(queryClientName)
                                .relatedClientId(existing.getId())
                                .relatedClientName(existing.getFullName())
                                .conflictType(ConflictCheck.ConflictType.CLIENT.name())
                                .matchField("client_name")
                                .matchValue(queryClientName)
                                .severity(ConflictCheck.Severity.HIGH.name())
                                .isConflict(true)
                                .checkedAt(now)
                                .build();
                        conflicts.add(cc);
                    } else {
                        // Word-level fuzzy match
                        String[] qw = qn.split("\\s+");
                        String[] ew = en.split("\\s+");
                        long matchCount = 0;
                        for (String wq : qw) {
                            if (wq.length() <= 3) continue;
                            for (String we : ew) {
                                if (we.length() > 3 && we.contains(wq)) {
                                    matchCount++;
                                    break;
                                }
                            }
                        }
                        if (matchCount >= Math.max(1, qw.length / 2.0)) {
                            ConflictCheck cc = ConflictCheck.builder()
                                    .lawFirmId(lawFirmId)
                                    .queryClientName(queryClientName)
                                    .relatedClientId(existing.getId())
                                    .relatedClientName(existing.getFullName())
                                    .conflictType(ConflictCheck.ConflictType.CLIENT.name())
                                    .matchField("client_name_fuzzy")
                                    .matchValue(queryClientName)
                                    .severity(ConflictCheck.Severity.MEDIUM.name())
                                    .isConflict(true)
                                    .checkedAt(now)
                                    .build();
                            conflicts.add(cc);
                        }
                    }
                }
            }
        }

        // 2. Check by email overlap
        if (clientId != null) {
            Client prospective = clientRepository.findById(clientId).orElse(null);
            if (prospective != null && prospective.getEmail() != null) {
                for (Client existing : allClients) {
                    if (!existing.getId().equals(clientId)
                            && existing.getEmail() != null
                            && existing.getEmail().equalsIgnoreCase(prospective.getEmail())) {
                        ConflictCheck cc = ConflictCheck.builder()
                                .lawFirmId(lawFirmId)
                                .queryClientName(queryClientName)
                                .relatedClientId(existing.getId())
                                .relatedClientName(existing.getFullName())
                                .conflictType(ConflictCheck.ConflictType.CLIENT.name())
                                .matchField("email")
                                .matchValue(prospective.getEmail())
                                .severity(ConflictCheck.Severity.CRITICAL.name())
                                .isConflict(true)
                                .checkedAt(now)
                                .build();
                        conflicts.add(cc);
                    }
                }
            }
        }

        // 3. Check opposing party overlap via cases
        if (queryCaseId != null) {
            Case prospectiveCase = caseRepository.findById(queryCaseId).orElse(null);
            if (prospectiveCase != null) {
                // Find other active cases for the same client at this firm
                List<Case> allCases = caseRepository.findAll().stream()
                        .filter(c -> c.getLawFirmId().equals(lawFirmId) && c.getDeletedAt() == null)
                        .filter(c -> !c.getId().equals(queryCaseId))
                        .filter(c -> clientId != null && clientId.equals(c.getClientId()))
                        .toList();
                for (Case existingCase : allCases) {
                    ConflictCheck cc = ConflictCheck.builder()
                            .lawFirmId(lawFirmId)
                            .queryCaseId(queryCaseId)
                            .queryClientName(queryClientName)
                            .relatedCaseId(existingCase.getId())
                            .relatedClientId(existingCase.getClientId())
                            .relatedClientName(existingCase.getClientName())
                            .conflictType(ConflictCheck.ConflictType.PARTY.name())
                            .matchField("case_client_overlap")
                            .matchValue(queryClientName)
                            .severity(ConflictCheck.Severity.HIGH.name())
                            .isConflict(true)
                            .checkedAt(now)
                            .build();
                    conflicts.add(cc);
                }
            }
        }

        return conflicts;
    }

    /**
     * Save a conflict check result.
     */
    @Transactional
    public ConflictCheck saveCheck(ConflictCheck check) {
        return conflictCheckRepository.save(check);
    }

    /**
     * Clear a conflict (with justification).
     */
    @Transactional
    public ConflictCheck clearConflict(String checkId, String clearedByUserId, String reason) {
        ConflictCheck check = conflictCheckRepository.findById(checkId)
                .orElseThrow(() -> new IllegalArgumentException("Conflict check not found: " + checkId));
        check.setCleared(true);
        check.setClearedByUserId(clearedByUserId);
        check.setClearedReason(reason);
        return conflictCheckRepository.save(check);
    }

    /**
     * Get all active (uncleared) conflicts for a firm.
     */
    @Transactional(readOnly = true)
    public List<ConflictCheck> getActiveConflicts(String lawFirmId) {
        return conflictCheckRepository.findActiveConflicts(lawFirmId);
    }

    /**
     * Get all conflict checks for a client.
     */
    @Transactional(readOnly = true)
    public List<ConflictCheck> getChecksForClient(String lawFirmId, String clientId) {
        return conflictCheckRepository.findByQueryClientId(lawFirmId, clientId);
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase();
    }
}