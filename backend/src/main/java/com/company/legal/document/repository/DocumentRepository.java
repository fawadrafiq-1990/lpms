package com.company.legal.document.repository;

import com.company.legal.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Document entity.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    List<Document> findByCaseId(String caseId);

    List<Document> findByClientId(String clientId);

    Page<Document> findByLawyerId(String lawyerId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.caseId = :caseId")
    Page<Document> findByCaseId(@Param("caseId") String caseId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.clientId = :clientId")
    Page<Document> findByClientId(@Param("clientId") String clientId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.category = :category")
    List<Document> findByCategory(@Param("category") String category);

    long countByCaseId(String caseId);

    long countByClientId(String clientId);
}