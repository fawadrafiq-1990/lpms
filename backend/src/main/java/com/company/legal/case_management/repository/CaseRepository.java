package com.company.legal.case_management.repository;

import com.company.legal.case_management.entity.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Case entity.
 */
@Repository
public interface CaseRepository extends JpaRepository<Case, String> {

    Optional<Case> findByCaseNumber(String caseNumber);

    boolean existsByCaseNumber(String caseNumber);

    Page<Case> findByLawFirmId(String lawFirmId, Pageable pageable);

    Page<Case> findByLawFirmIdAndCaseStatus(
            String lawFirmId, String status, Pageable pageable);

    @Query("SELECT c FROM Case c WHERE c.lawFirmId = :lawFirmId " +
           "AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.caseNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR c.filingNumber LIKE CONCAT('%', :search, '%'))")
    Page<Case> searchByLawFirm(@Param("lawFirmId") String lawFirmId,
                               @Param("search") String search,
                               Pageable pageable);

    @Query("SELECT c FROM Case c WHERE c.clientId = :clientId")
    List<Case> findByClientId(@Param("clientId") String clientId);

    @Query("SELECT c FROM Case c WHERE c.leadLawyerId = :lawyerId")
    List<Case> findByLeadLawyerId(@Param("lawyerId") String lawyerId);

    @Query("SELECT c FROM Case c WHERE c.caseStatus = 'IN_PROGRESS'")
    List<Case> findInProgressCases();

    long countByLawFirmId(String lawFirmId);

    long countByLawFirmIdAndCaseStatus(String lawFirmId, String status);
}