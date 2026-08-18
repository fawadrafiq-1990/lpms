package com.company.legal.hearing.repository;

import com.company.legal.hearing.entity.Hearing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Hearing entity.
 */
@Repository
public interface HearingRepository extends JpaRepository<Hearing, String> {

    @Query("SELECT h FROM Hearing h WHERE h.caseId = :caseId")
    List<Hearing> findByCaseId(@Param("caseId") String caseId);

    @Query("SELECT h FROM Hearing h WHERE h.scheduledDate >= :from AND h.scheduledDate <= :to AND h.status = 'SCHEDULED'")
    List<Hearing> findUpcomingHearings(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT h FROM Hearing h WHERE h.caseId = :caseId AND h.status = 'SCHEDULED'")
    List<Hearing> findUpcomingByCaseId(@Param("caseId") String caseId);

    @Query("SELECT h FROM Hearing h WHERE h.status = 'SCHEDULED' AND h.scheduledDate BETWEEN :today AND :tomorrow")
    List<Hearing> findTodayAndTomorrowHearings(@Param("today") LocalDate today, @Param("tomorrow") LocalDate tomorrow);

    Page<Hearing> findByCaseId(String caseId, Pageable pageable);

    List<Hearing> findByCourtId(String courtId);

    List<Hearing> findByJudgeId(String judgeId);
}