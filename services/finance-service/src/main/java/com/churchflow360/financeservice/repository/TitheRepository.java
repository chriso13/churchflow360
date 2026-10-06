package com.churchflow360.financeservice.repository;

import com.churchflow360.financeservice.model.Tithe;
import com.churchflow360.financeservice.model.TithePeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TitheRepository extends JpaRepository<Tithe, UUID> {

    // Member's tithes — most recent first
    Page<Tithe> findByMembershipNumberOrderByPaymentDateDesc(
            String membershipNumber, Pageable pageable);

    // All tithes for a member in a given month/year (list — no longer unique)
    List<Tithe> findByMembershipNumberAndTitheMonthAndTitheYearOrderByPaymentDateAsc(
            String membershipNumber, TithePeriod titheMonth, Integer titheYear);

    // All tithes in a date range (used for summaries — sum done in Java)
    List<Tithe> findByPaymentDateBetween(LocalDate from, LocalDate to);

    // Paginated date range query
    Page<Tithe> findByPaymentDateBetweenOrderByPaymentDateDesc(
            LocalDate from, LocalDate to, Pageable pageable);

    // All tithes for a member (used for yearly total — sum done in Java)
    List<Tithe> findByMembershipNumber(String membershipNumber);

    // All tithes for a specific year (used for monthly breakdown)
    List<Tithe> findByTitheYear(Integer titheYear);

    // All tithes for a specific year and month
    List<Tithe> findByTitheYearAndTitheMonth(Integer titheYear, TithePeriod titheMonth);
}