package com.churchflow360.financeservice.repository;

import com.churchflow360.financeservice.model.Offering;
import com.churchflow360.financeservice.model.OfferingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface OfferingRepository extends JpaRepository<Offering, UUID> {

    List<Offering> findByServiceDateOrderByOfferingTypeAsc(LocalDate serviceDate);

    Page<Offering> findByServiceDateBetweenOrderByServiceDateDesc(
            LocalDate from, LocalDate to, Pageable pageable);

    Page<Offering> findByOfferingTypeOrderByServiceDateDesc(
            OfferingType offeringType, Pageable pageable);

    List<Offering> findByVerifiedAtIsNullOrderByServiceDateAsc();

    // Total offering collected in a date range
    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0)
            FROM Offering o
            WHERE o.serviceDate BETWEEN :from AND :to
            """)
    BigDecimal sumByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // Monthly offering totals for a year
    @Query(value = """
            SELECT EXTRACT(MONTH FROM service_date) as month,
                   COALESCE(SUM(total_amount), 0) as total
            FROM offerings
            WHERE EXTRACT(YEAR FROM service_date) = :year
            GROUP BY EXTRACT(MONTH FROM service_date)
            ORDER BY month
            """, nativeQuery = true)
    List<Object[]> monthlyTotalsByYear(@Param("year") Integer year);

    // Totals by type in a date range
    @Query("""
            SELECT o.offeringType, COALESCE(SUM(o.totalAmount), 0)
            FROM Offering o
            WHERE o.serviceDate BETWEEN :from AND :to
            GROUP BY o.offeringType
            ORDER BY SUM(o.totalAmount) DESC
            """)
    List<Object[]> totalByType(@Param("from") LocalDate from, @Param("to") LocalDate to);
}