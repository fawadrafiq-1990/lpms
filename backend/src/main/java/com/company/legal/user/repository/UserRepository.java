package com.company.legal.user.repository;

import com.company.legal.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findById(String id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, String id);

    Page<User> findByLawFirmId(String lawFirmId, Pageable pageable);

    Page<User> findByBranchId(String branchId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.lawFirmId = :lawFirmId " +
           "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchByLawFirm(@Param("lawFirmId") String lawFirmId,
                               @Param("search") String search,
                               Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName " +
           "AND u.lawFirmId = :lawFirmId")
    List<User> findByLawFirmIdAndRoleName(@Param("lawFirmId") String lawFirmId,
                                          @Param("roleName") String roleName);

    @Query("SELECT COUNT(u) FROM User u WHERE u.lawFirmId = :lawFirmId")
    long countByLawFirmId(@Param("lawFirmId") String lawFirmId);

    @Query("SELECT u FROM User u WHERE u.lawFirmId = :lawFirmId AND u.status = 'ACTIVE'")
    List<User> findActiveByLawFirmId(@Param("lawFirmId") String lawFirmId);
}