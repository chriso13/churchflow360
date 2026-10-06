package com.churchflow360.attendanceservice.repository;

import com.churchflow360.attendanceservice.model.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, UUID> {

    List<AttendanceRecord> findByServiceEventIdOrderByCheckedInAtAsc(UUID serviceEventId);

    boolean existsByServiceEventIdAndMembershipNumber(UUID serviceEventId, String membershipNumber);

    Optional<AttendanceRecord> findByServiceEventIdAndMembershipNumber(
            UUID serviceEventId, String membershipNumber);

    long countByServiceEventId(UUID serviceEventId);

    // Member attendance history — all services a membership number attended
    Page<AttendanceRecord> findByMembershipNumberOrderByCheckedInAtDesc(
            String membershipNumber, Pageable pageable);

    // Count how many services a member attended in a date range
    @Query("""
            SELECT COUNT(a) FROM AttendanceRecord a
            JOIN a.serviceEvent e
            WHERE a.membershipNumber = :membershipNumber
            AND e.serviceDate BETWEEN :from AND :to
            """)
    long countMemberAttendanceInRange(@Param("membershipNumber") String membershipNumber,
                                      @Param("from") LocalDate from,
                                      @Param("to") LocalDate to);

    // Top attending members in a period
    @Query("""
            SELECT a.membershipNumber, a.memberName, COUNT(a) as total
            FROM AttendanceRecord a
            JOIN a.serviceEvent e
            WHERE e.serviceDate BETWEEN :from AND :to
            GROUP BY a.membershipNumber, a.memberName
            ORDER BY total DESC
            """)
    List<Object[]> findTopAttendees(@Param("from") LocalDate from,
                                    @Param("to") LocalDate to,
                                    Pageable pageable);
}