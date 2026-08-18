package com.company.legal.billing.repository;

import com.company.legal.billing.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Invoice entity.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    @Query("SELECT i FROM Invoice i WHERE i.lawFirmId = :lawFirmId")
    Page<Invoice> findByLawFirmId(@Param("lawFirmId") String lawFirmId, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.caseId = :caseId")
    List<Invoice> findByCaseId(@Param("caseId") String caseId);

    @Query("SELECT i FROM Invoice i WHERE i.clientId = :clientId")
    List<Invoice> findByClientId(@Param("clientId") String clientId);

    @Query("SELECT i FROM Invoice i WHERE i.status = :status AND i.lawFirmId = :lawFirmId")
    Page<Invoice> findByStatus(@Param("lawFirmId") String lawFirmId,
                               @Param("status") String status,
                               Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :date AND i.status NOT IN ('PAID', 'CANCELLED')")
    List<Invoice> findOverdueInvoices(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(i.amountOutstanding), 0) FROM Invoice i WHERE i.lawFirmId = :lawFirmId AND i.status NOT IN ('PAID', 'CANCELLED')")
    BigDecimal getTotalOutstanding(@Param("lawFirmId") String lawFirmId);

    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.lawFirmId = :lawFirmId " +
           "AND i.invoiceDate >= :start AND i.invoiceDate <= :end AND i.status = 'PAID'")
    BigDecimal getRevenueInRange(@Param("lawFirmId") String lawFirmId,
                                 @Param("start") LocalDate start,
                                 @Param("end") LocalDate end);
}