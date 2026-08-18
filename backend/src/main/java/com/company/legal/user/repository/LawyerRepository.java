package com.company.legal.user.repository;

import com.company.legal.user.entity.Lawyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Lawyer} profiles.
 */
@Repository
public interface LawyerRepository extends JpaRepository<Lawyer, String> {

    Optional<Lawyer> findByUserId(String userId);

    List<Lawyer> findByLawFirmId(String lawFirmId);

    @Query("SELECT l FROM Lawyer l WHERE l.lawFirmId = :lawFirmId AND l.availabilityStatus = 'AVAILABLE'")
    List<Lawyer> findAvailableByLawFirmId(@Param("lawFirmId") String lawFirmId);

    boolean existsByUserId(String userId);
}
