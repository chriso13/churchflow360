package com.churchflow360.financeservice.service;

import com.churchflow360.financeservice.exception.InsufficientFundsException;
import com.churchflow360.financeservice.model.ExpenseStatus;
import com.churchflow360.financeservice.repository.DonationRepository;
import com.churchflow360.financeservice.repository.ExpenseRepository;
import com.churchflow360.financeservice.repository.OfferingRepository;
import com.churchflow360.financeservice.repository.TitheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FundBalanceService {

    private final TitheRepository    titheRepository;
    private final DonationRepository donationRepository;
    private final OfferingRepository offeringRepository;
    private final ExpenseRepository  expenseRepository;

    private static final LocalDate EPOCH = LocalDate.of(2000, 1, 1);

    public BigDecimal getTotalIncome() {
        // Sum all tithes in Java
        BigDecimal tithes = titheRepository.findAll().stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sum all donations in Java
        BigDecimal donations = donationRepository.findAll().stream()
                .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sum all offerings in Java
        BigDecimal offerings = offeringRepository.findAll().stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return tithes.add(donations).add(offerings);
    }

    public BigDecimal getTotalCommitted() {
        // Sum PAID expenses
        BigDecimal paid = expenseRepository.findByStatus(ExpenseStatus.PAID).stream()
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sum APPROVED (reserved) expenses
        BigDecimal approved = expenseRepository.findByStatus(ExpenseStatus.APPROVED).stream()
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return paid.add(approved);
    }

    public BigDecimal getAvailableBalance() {
        BigDecimal income    = getTotalIncome();
        BigDecimal committed = getTotalCommitted();
        BigDecimal balance   = income.subtract(committed);
        log.debug("Balance — income: {} committed: {} available: {}", income, committed, balance);
        return balance;
    }

    public void assertSufficientFunds(BigDecimal expenseAmount) {
        BigDecimal available = getAvailableBalance();
        log.info("Fund check — expense: ${} | available: ${}", expenseAmount, available);

        if (expenseAmount.compareTo(available) > 0) {
            throw new InsufficientFundsException(String.format(
                    "Insufficient funds. Expense amount $%.2f exceeds the available " +
                            "balance of $%.2f. Total income: $%.2f | Total committed: $%.2f.",
                    expenseAmount, available, getTotalIncome(), getTotalCommitted()));
        }
    }
}