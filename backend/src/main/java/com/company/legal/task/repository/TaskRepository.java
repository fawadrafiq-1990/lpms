package com.company.legal.task.repository;

import com.company.legal.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByCaseId(String caseId);
    List<Task> findByAssignedTo(String assignedTo);
    Page<Task> findByAssignedTo(String assignedTo, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.assignedTo = :assignedTo AND t.status != 'COMPLETED' AND t.status != 'CANCELLED'")
    List<Task> findPendingTasksByAssignee(@Param("assignedTo") String assignedTo);
    
    @Query("SELECT t FROM Task t WHERE t.caseId = :caseId")
    Page<Task> findByCaseId(@Param("caseId") String caseId, Pageable pageable);
}
