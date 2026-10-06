package com.churchflow360.attendanceservice.repository;

import com.churchflow360.attendanceservice.model.ServiceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceEventRepository extends JpaRepository<ServiceEvent, UUID> {

    List<ServiceEvent> findByServiceDateOrderByStartTimeAsc(LocalDate date);

    @Query("SELECT e FROM ServiceEvent e WHERE e.serviceDate BETWEEN :from AND :to " +
            "ORDER BY e.serviceDate ASC, e.startTime ASC")
    List<ServiceEvent> findByDateRange(
            @Param("from") LocalDate from,
            @Param("to")   LocalDate to);

    Optional<ServiceEvent> findByQrToken(String qrToken);

    /**
     * Detects time overlap at the same location on the same date.
     *
     * Two events overlap when:
     *   newStart < existingEnd  AND  newEnd > existingStart
     *
     * This covers all overlap cases:
     *   - New event starts inside existing   (newStart >= existStart AND newStart < existEnd)
     *   - New event ends inside existing     (newEnd > existStart AND newEnd <= existEnd)
     *   - New event completely wraps existing (newStart <= existStart AND newEnd >= existEnd)
     *
     * @param location   location to check (case-insensitive)
     * @param date       service date
     * @param startTime  proposed start time
     * @param endTime    proposed end time
     * @param excludeId  UUID to exclude when updating an existing event (pass null for new)
     */
    @Query("""
           SELECT e FROM ServiceEvent e
           WHERE LOWER(e.location) = LOWER(:location)
             AND e.serviceDate = :date
             AND e.startTime  < :endTime
             AND e.endTime    > :startTime
             AND (:excludeId IS NULL OR e.id <> :excludeId)
           """)
    List<ServiceEvent> findOverlappingEvents(
            @Param("location")  String    location,
            @Param("date")      LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime")   LocalTime endTime,
            @Param("excludeId") UUID      excludeId);
}