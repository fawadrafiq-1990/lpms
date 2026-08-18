package com.company.legal.conflict.repository;

import com.company.legal.conflict.entity.ConflictCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConflictCheckRepository extends JpaRepository<ConflictCheck, String> {

    @Query("SELECT c FROM ConflictCheck c WHERE c.lawFirmId = :lawFirmId " +
           "AND c.isConflict = true AND c.cleared = false")
    List<ConflictCheck> findActiveConflicts(@Param("lawFirmId") String lawFirmId);

    @Query("SELECT c FROM ConflictCheck c WHERE c.lawFirmId = :lawFirmId " +
           "AND c.relatedClientId = :clientId")
    List<ConflictCheck> findByRelatedClientId(@Param("lawFirmId") String lawFirmId,
                                               @Param("clientId") String clientId);

    @Query("SELECT c FROM ConflictCheck c WHERE c.lawFirmId = :lawFirmId " +
           "AND c.queryClientId = :clientId")
    List<ConflictCheck> findByQueryClientId(@Param("lawFirmId") String lawFirmId,
                                             @Param("clientId") String clientId);

    @Query("SELECT c FROM ConflictCheck c WHERE c.lawFirmId = :lawFirmId " +
           "ORDER BY c.checkedAt DESC")
    List<ConflictCheck> findAllRecent(@Param("lawFirmId") String lawFirmId);
}