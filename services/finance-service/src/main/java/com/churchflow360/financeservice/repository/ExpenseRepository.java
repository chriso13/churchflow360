package com.churchflow360.financeservice.repository;

import com.churchflow360.financeservice.model.Expense;
import com.churchflow360.financeservice.model.ExpenseCategory;
import com.churchflow360.financeservice.model.ExpenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    Page<Expense> findByStatusOrderByExpenseDateDesc(ExpenseStatus status, Pageable pageable);
    Page<Expense> findByCategoryOrderByExpenseDateDesc(ExpenseCategory category, Pageable pageable);
    Page<Expense> findBySubmittedByOrderByCreatedAtDesc(String submittedBy, Pageable pageable);
    Page<Expense> findByExpenseDateBetweenOrderByExpenseDateDesc(LocalDate from, LocalDate to, Pageable pageable);
    List<Expense> findByStatusOrderBySubmittedAtAsc(ExpenseStatus status);
    List<Expense> findByStatus(ExpenseStatus status);
    List<Expense> findByDueDateBeforeAndStatusNotIn(LocalDate today, List<ExpenseStatus> excludedStatuses);
    long countByStatus(ExpenseStatus status);
}
