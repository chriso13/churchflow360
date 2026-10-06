package com.churchflow360.financeservice.repository;

import com.churchflow360.financeservice.model.Donation;
import com.churchflow360.financeservice.model.DonationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {

    Page<Donation> findByMembershipNumberOrderByPaymentDateDesc(
            String membershipNumber, Pageable pageable);

    Page<Donation> findByCategoryOrderByPaymentDateDesc(
            DonationCategory category, Pageable pageable);

    Page<Donation> findByPaymentDateBetweenOrderByPaymentDateDesc(
            LocalDate from, LocalDate to, Pageable pageable);

    List<Donation> findByReceiptIssuedFalseAndMembershipNumberIsNotNull();

    Optional<Donation> findByReceiptNumber(String receiptNumber);

    // Total donations by category in a date range
    @Query("""
            SELECT d.category, COALESCE(SUM(d.amount), 0)
            FROM Donation d
            WHERE d.paymentDate BETWEEN :from AND :to
            GROUP BY d.category
            ORDER BY SUM(d.amount) DESC
            """)
    List<Object[]> totalByCategory(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // Grand total in a date range
    @Query("""
            SELECT COALESCE(SUM(d.amount), 0) FROM Donation d
            WHERE d.paymentDate BETWEEN :from AND :to
            """)
    BigDecimal sumByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // Total by specific category in a date range
    @Query("""
            SELECT COALESCE(SUM(d.amount), 0) FROM Donation d
            WHERE d.category = :category AND d.paymentDate BETWEEN :from AND :to
            """)
    BigDecimal sumByCategoryAndDateRange(@Param("category") DonationCategory category,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to);

    long countByIsAnonymousTrue();
}