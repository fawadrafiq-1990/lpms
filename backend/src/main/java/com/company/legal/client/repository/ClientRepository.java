package com.company.legal.client.repository;

import com.company.legal.client.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Client entity.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    Optional<Client> findByClientNumber(String clientNumber);

    boolean existsByClientNumber(String clientNumber);

    boolean existsByEmailAndIdNot(String email, String id);

    boolean existsByCompanyName(String companyName);

    Page<Client> findByLawFirmId(String lawFirmId, Pageable pageable);

    Page<Client> findByLawFirmIdAndStatus(
            String lawFirmId, Client.ClientStatus status, Pageable pageable);

    @Query("SELECT c FROM Client c WHERE c.lawFirmId = :lawFirmId " +
           "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR c.clientNumber LIKE CONCAT('%', :search, '%'))")
    Page<Client> searchByLawFirm(@Param("lawFirmId") String lawFirmId,
                                 @Param("search") String search,
                                 Pageable pageable);

    @Query("SELECT c FROM Client c WHERE c.lawFirmId = :lawFirmId " +
           "AND c.clientType = :clientType")
    Page<Client> findByLawFirmIdAndClientType(@Param("lawFirmId") String lawFirmId,
                                              @Param("clientType") Client.ClientType clientType,
                                              Pageable pageable);

    @Query("SELECT c FROM Client c WHERE c.assignedLawyerId = :lawyerId")
    List<Client> findByAssignedLawyerId(@Param("lawyerId") String lawyerId);

    @Query("SELECT c FROM Client c WHERE c.lawFirmId = :lawFirmId AND c.status = 'ACTIVE'")
    List<Client> findActiveByLawFirmId(@Param("lawFirmId") String lawFirmId);

    long countByLawFirmId(String lawFirmId);

    long countByLawFirmIdAndStatus(
            String lawFirmId, Client.ClientStatus status);
}