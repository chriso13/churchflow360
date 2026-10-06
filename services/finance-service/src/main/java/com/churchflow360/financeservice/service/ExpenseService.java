package com.churchflow360.financeservice.service;

import com.churchflow360.financeservice.client.MemberClient;
import com.churchflow360.financeservice.dto.request.*;
import com.churchflow360.financeservice.dto.response.*;
import com.churchflow360.financeservice.exception.FinanceOperationException;
import com.churchflow360.financeservice.exception.ResourceNotFoundException;
import com.churchflow360.financeservice.mapper.ExpenseMapper;
import com.churchflow360.financeservice.model.*;
import com.churchflow360.financeservice.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository  expenseRepository;
    private final ExpenseMapper      expenseMapper;
    private final MemberClient       memberClient;
    private final FundBalanceService fundBalanceService;

    // ── Create ─────────────────────────────────────────────────────────────────

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        // Verify submittedBy exists
        memberClient.verifyMemberExists(request.getSubmittedBy());

        // Verify recordedBy exists — may be different from submittedBy
        if (!request.getRecordedBy().equalsIgnoreCase(request.getSubmittedBy())) {
            memberClient.verifyMemberExists(request.getRecordedBy());
        }

        Expense expense = expenseMapper.toEntity(request);
        expense.setSubmittedBy(request.getSubmittedBy().toUpperCase());
        expense.setRecordedBy(request.getRecordedBy().toUpperCase());
        expense.setStatus(ExpenseStatus.DRAFT);

        Expense saved = expenseRepository.save(expense);
        log.info("Expense created: {} — ${} [{}]",
                saved.getTitle(), saved.getAmount(), saved.getCategory());
        return expenseMapper.toResponse(saved);
    }

    // ── Read ───────────────────────────────────────────────────────────────────

    public ExpenseResponse getExpense(UUID id) {
        return expenseMapper.toResponse(findOrThrow(id));
    }

    public PagedResponse<ExpenseResponse> getByStatus(ExpenseStatus status, Pageable pageable) {
        return PagedResponse.of(
                expenseRepository.findByStatusOrderByExpenseDateDesc(status, pageable)
                        .map(expenseMapper::toResponse));
    }

    public PagedResponse<ExpenseResponse> getByCategory(ExpenseCategory category, Pageable pageable) {
        return PagedResponse.of(
                expenseRepository.findByCategoryOrderByExpenseDateDesc(category, pageable)
                        .map(expenseMapper::toResponse));
    }

    public PagedResponse<ExpenseResponse> getBySubmitter(String membershipNumber, Pageable pageable) {
        memberClient.verifyMemberExists(membershipNumber);
        return PagedResponse.of(
                expenseRepository.findBySubmittedByOrderByCreatedAtDesc(
                                membershipNumber.toUpperCase(), pageable)
                        .map(expenseMapper::toResponse));
    }

    public PagedResponse<ExpenseResponse> getByDateRange(
            LocalDate from, LocalDate to, Pageable pageable) {
        return PagedResponse.of(
                expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDesc(from, to, pageable)
                        .map(expenseMapper::toResponse));
    }

    public List<ExpenseResponse> getPendingApproval() {
        return expenseRepository.findByStatusOrderBySubmittedAtAsc(ExpenseStatus.SUBMITTED)
                .stream().map(expenseMapper::toResponse).toList();
    }

    public List<ExpenseResponse> getOverdue() {
        List<ExpenseStatus> excluded = List.of(
                ExpenseStatus.PAID, ExpenseStatus.CANCELLED, ExpenseStatus.REJECTED);
        return expenseRepository
                .findByDueDateBeforeAndStatusNotIn(LocalDate.now(), excluded)
                .stream().map(expenseMapper::toResponse).toList();
    }

    // ── Workflow ───────────────────────────────────────────────────────────────

    @Transactional
    public ExpenseResponse submit(UUID id) {
        Expense expense = findOrThrow(id);
        validateTransition(expense, ExpenseStatus.SUBMITTED);
        expense.setStatus(ExpenseStatus.SUBMITTED);
        expense.setSubmittedAt(LocalDateTime.now());
        log.info("Expense submitted: {} — ${}", expense.getTitle(), expense.getAmount());
        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse approve(UUID id, ApproveExpenseRequest request) {
        Expense expense = findOrThrow(id);
        validateTransition(expense, ExpenseStatus.APPROVED);

        // Verify approvedBy exists in member-service
        memberClient.verifyMemberExists(request.getApprovedBy());

        // Separation of duties — approver cannot be the submitter
        if (expense.getSubmittedBy().equalsIgnoreCase(request.getApprovedBy())) {
            throw new FinanceOperationException(
                    "The submitter (" + expense.getSubmittedBy() +
                            ") cannot approve their own expense. A different member must approve.");
        }

        // Fund balance guard
        fundBalanceService.assertSufficientFunds(expense.getAmount());

        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setApprovedBy(request.getApprovedBy().toUpperCase());
        expense.setApprovedAt(LocalDateTime.now());
        if (request.getNotes() != null) expense.setNotes(request.getNotes());

        log.info("Expense approved: {} — ${} by {}",
                expense.getTitle(), expense.getAmount(), request.getApprovedBy());
        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse reject(UUID id, RejectExpenseRequest request) {
        Expense expense = findOrThrow(id);
        validateTransition(expense, ExpenseStatus.REJECTED);

        // Verify rejectedBy exists in member-service
        memberClient.verifyMemberExists(request.getRejectedBy());

        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setApprovedBy(request.getRejectedBy().toUpperCase());
        expense.setApprovedAt(LocalDateTime.now());
        expense.setRejectionReason(request.getReason());

        log.info("Expense rejected: {} — reason: {}", expense.getTitle(), request.getReason());
        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse markPaid(UUID id, MarkPaidRequest request) {
        Expense expense = findOrThrow(id);
        validateTransition(expense, ExpenseStatus.PAID);
        expense.setStatus(ExpenseStatus.PAID);
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setPaymentReference(request.getPaymentReference());
        expense.setPaidDate(request.getPaidDate() != null
                ? request.getPaidDate() : LocalDate.now());
        if (request.getNotes() != null) expense.setNotes(request.getNotes());
        log.info("Expense paid: {} — ${}", expense.getTitle(), expense.getAmount());
        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse cancel(UUID id, String reason) {
        Expense expense = findOrThrow(id);
        validateTransition(expense, ExpenseStatus.CANCELLED);
        expense.setStatus(ExpenseStatus.CANCELLED);
        expense.setRejectionReason(reason);
        log.info("Expense cancelled: {}", expense.getTitle());
        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    // ── Balance & Summary ──────────────────────────────────────────────────────

    public FundBalanceResponse getBalance() {
        BigDecimal income    = fundBalanceService.getTotalIncome();
        BigDecimal committed = fundBalanceService.getTotalCommitted();
        return FundBalanceResponse.builder()
                .totalIncome(income)
                .totalCommitted(committed)
                .availableBalance(income.subtract(committed))
                .build();
    }

    public ExpenseSummaryResponse getSummary(LocalDate from, LocalDate to) {
        BigDecimal totalPaid = expenseRepository
                .findByExpenseDateBetweenOrderByExpenseDateDesc(from, to, Pageable.unpaged())
                .stream()
                .filter(e -> ExpenseStatus.PAID.equals(e.getStatus()))
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ExpenseStatus> excluded = List.of(
                ExpenseStatus.PAID, ExpenseStatus.CANCELLED, ExpenseStatus.REJECTED);
        long overdueCount = expenseRepository
                .findByDueDateBeforeAndStatusNotIn(LocalDate.now(), excluded).size();

        return ExpenseSummaryResponse.builder()
                .from(from).to(to)
                .totalPaid(totalPaid)
                .totalPending(BigDecimal.ZERO)
                .totalDraft(BigDecimal.ZERO)
                .countPaid(expenseRepository.countByStatus(ExpenseStatus.PAID))
                .countPending(expenseRepository.countByStatus(ExpenseStatus.SUBMITTED)
                        + expenseRepository.countByStatus(ExpenseStatus.APPROVED))
                .countDraft(expenseRepository.countByStatus(ExpenseStatus.DRAFT))
                .countOverdue(overdueCount)
                .countRejected(expenseRepository.countByStatus(ExpenseStatus.REJECTED))
                .build();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Expense findOrThrow(UUID id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expense not found with id: " + id));
    }

    private void validateTransition(Expense expense, ExpenseStatus target) {
        if (!expense.canTransitionTo(target)) {
            throw new FinanceOperationException(
                    "Cannot transition expense from " + expense.getStatus() +
                            " to " + target + ". " + getAllowed(expense.getStatus()));
        }
    }

    private String getAllowed(ExpenseStatus from) {
        return switch (from) {
            case DRAFT     -> "Allowed: SUBMITTED, CANCELLED";
            case SUBMITTED -> "Allowed: APPROVED, REJECTED, CANCELLED";
            case APPROVED  -> "Allowed: PAID, CANCELLED";
            default        -> "No further transitions allowed.";
        };
    }
}