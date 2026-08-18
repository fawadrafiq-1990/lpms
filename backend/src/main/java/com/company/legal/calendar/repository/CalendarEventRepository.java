package com.company.legal.calendar.repository;

import com.company.legal.calendar.entity.CalendarEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for {@link CalendarEvent}.
 */
@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, String> {

    List<CalendarEvent> findByUserId(String userId);

    @Query("SELECT e FROM CalendarEvent e WHERE e.userId = :userId " +
           "AND e.startDate <= :rangeEnd AND e.endDate >= :rangeStart " +
           "ORDER BY e.startDate ASC")
    List<CalendarEvent> findForUserInRange(@Param("userId") String userId,
                                           @Param("rangeStart") LocalDateTime rangeStart,
                                           @Param("rangeEnd") LocalDateTime rangeEnd);

    Page<CalendarEvent> findByUserId(String userId, Pageable pageable);

    boolean existsByEventNumber(String eventNumber);
}
