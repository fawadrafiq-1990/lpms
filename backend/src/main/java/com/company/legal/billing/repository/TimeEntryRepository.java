package com.company.legal.billing.repository;

import com.company.legal.billing.entity.TimeEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for TimeEntry entity.
 */
@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, String> {

    @Query("SELECT t FROM TimeEntry t WHERE t.caseId = :caseId")
    List<TimeEntry> findByCaseId(@Param("caseId") String caseId);

    @Query("SELECT t FROM TimeEntry t WHERE t.userId = :userId")
    Page<TimeEntry> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT t FROM TimeEntry t WHERE t.userId = :userId AND t.billable = true AND t.billed = false")
    List<TimeEntry> findUnbilledBillableEntries(@Param("userId") String userId);

    @Query("SELECT t FROM TimeEntry t WHERE t.workDate BETWEEN :startDate AND :endDate")
    Page<TimeEntry> findByDateRange(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.durationHours), 0) FROM TimeEntry t WHERE t.caseId = :caseId AND t.billable = true")
    BigDecimal getTotalBillableHoursForCase(@Param("caseId") String caseId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TimeEntry t WHERE t.userId = :userId AND t.workDate BETWEEN :startDate AND :endDate AND t.billable = true")
    BigDecimal getBillableAmountByUser(@Param("userId") String userId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    @Modifying
    @Query("UPDATE TimeEntry t SET t.billed = true WHERE t.id IN :ids")
    void markAsBilled(@Param("ids") List<String> ids);
}