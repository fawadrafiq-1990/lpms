package com.company.legal.case_management.repository;

import com.company.legal.case_management.entity.CaseLawyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link CaseLawyer} assignments.
 */
@Repository
public interface CaseLawyerRepository extends JpaRepository<CaseLawyer, String> {

    List<CaseLawyer> findByCaseId(String caseId);

    Optional<CaseLawyer> findByCaseIdAndLawyerId(String caseId, String lawyerId);

    void deleteByCaseIdAndLawyerId(String caseId, String lawyerId);
}
