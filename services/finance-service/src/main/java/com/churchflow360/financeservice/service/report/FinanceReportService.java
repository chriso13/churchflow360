package com.churchflow360.financeservice.service.report;

import com.churchflow360.financeservice.dto.response.report.*;
import com.churchflow360.financeservice.exception.ResourceNotFoundException;
import com.churchflow360.financeservice.model.*;
import com.churchflow360.financeservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FinanceReportService {

    private static final String CHURCH_NAME        = "Rehoboth Pentecostal Church";
    private static final String CHURCH_ADDRESS     = "123 Faith Avenue, Kissimmee, FL 34741";
    private static final String CHURCH_PHONE       = "(407) 555-0100";
    private static final String CHURCH_EMAIL       = "finance@360church.org";
    private static final String TAX_EXEMPT_NUMBER  = "EIN: 12-3456789";

    private final TitheRepository        titheRepository;
    private final DonationRepository     donationRepository;
    private final OfferingRepository     offeringRepository;
    private final ExpenseRepository      expenseRepository;

    // ══════════════════════════════════════════════════════════════════════════
    // YEAR-END TITHE STATEMENT
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Generates a year-end tithe statement for a specific member.
     * Lists every tithe payment with monthly totals — suitable for tax filing.
     */
    public YearEndTitheStatementResponse generateYearEndTitheStatement(
            String membershipNumber, Integer year) {

        List<Tithe> tithes = titheRepository
                .findByMembershipNumber(membershipNumber.toUpperCase())
                .stream()
                .filter(t -> t.getTitheYear().equals(year))
                .sorted(Comparator.comparing(Tithe::getPaymentDate))
                .toList();

        if (tithes.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No tithe records found for member " + membershipNumber +
                            " in year " + year + ".");
        }

        String memberName = tithes.get(0).getMemberName();

        // Total
        BigDecimal total = tithes.stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Monthly breakdown
        Map<TithePeriod, List<Tithe>> byMonth = tithes.stream()
                .collect(Collectors.groupingBy(Tithe::getTitheMonth));

        List<YearEndTitheStatementResponse.MonthlyTitheSummary> monthlyBreakdown =
                Arrays.stream(TithePeriod.values())
                        .filter(byMonth::containsKey)
                        .map(month -> {
                            List<Tithe> monthTithes = byMonth.get(month);
                            BigDecimal monthTotal = monthTithes.stream()
                                    .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            return YearEndTitheStatementResponse.MonthlyTitheSummary.builder()
                                    .month(month.name())
                                    .year(year)
                                    .totalAmount(monthTotal)
                                    .paymentCount(monthTithes.size())
                                    .build();
                        })
                        .toList();

        // Individual line items
        List<YearEndTitheStatementResponse.TitheLineItem> lineItems = tithes.stream()
                .map(t -> YearEndTitheStatementResponse.TitheLineItem.builder()
                        .paymentDate(t.getPaymentDate())
                        .titheMonth(t.getTitheMonth().name())
                        .titheYear(t.getTitheYear())
                        .amount(t.getAmount())
                        .paymentMethod(t.getPaymentMethod().name())
                        .referenceNumber(t.getReferenceNumber())
                        .notes(t.getNotes())
                        .build())
                .toList();

        log.info("Year-end tithe statement generated: member={} year={} total=${}",
                membershipNumber, year, total);

        return YearEndTitheStatementResponse.builder()
                .membershipNumber(membershipNumber.toUpperCase())
                .memberName(memberName)
                .year(year)
                .generatedDate(LocalDate.now())
                .totalTithed(total)
                .numberOfPayments(tithes.size())
                .currency("USD")
                .monthlyBreakdown(monthlyBreakdown)
                .titheRecords(lineItems)
                .churchName(CHURCH_NAME)
                .churchAddress(CHURCH_ADDRESS)
                .churchPhone(CHURCH_PHONE)
                .churchEmail(CHURCH_EMAIL)
                .taxExemptNumber(TAX_EXEMPT_NUMBER)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // OFFERING REPORT
    // ══════════════════════════════════════════════════════════════════════════

    public OfferingReportResponse generateOfferingReport(LocalDate from, LocalDate to) {
        List<Offering> offerings = offeringRepository
                .findByServiceDateBetweenOrderByServiceDateDesc(from, to, Pageable.unpaged())
                .getContent();

        BigDecimal grandTotal = sum(offerings, o -> o.getTotalAmount());
        BigDecimal totalCash  = sum(offerings, o -> o.getCashAmount());
        BigDecimal totalCheck = sum(offerings, o -> o.getCheckAmount());
        BigDecimal totalDigital = sum(offerings, o -> o.getDigitalAmount());
        int totalGivers = offerings.stream().mapToInt(o ->
                o.getGiverCount() != null ? o.getGiverCount() : 0).sum();

        BigDecimal avgPerService = offerings.isEmpty() ? BigDecimal.ZERO
                : grandTotal.divide(BigDecimal.valueOf(offerings.size()), 2, RoundingMode.HALF_UP);

        // By offering type
        Map<OfferingType, List<Offering>> byType = offerings.stream()
                .collect(Collectors.groupingBy(Offering::getOfferingType));

        List<OfferingReportResponse.OfferingTypeSummary> typeSummaries = byType.entrySet().stream()
                .map(e -> {
                    BigDecimal typeTotal = sum(e.getValue(), Offering::getTotalAmount);
                    BigDecimal pct = grandTotal.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : typeTotal.divide(grandTotal, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                    return OfferingReportResponse.OfferingTypeSummary.builder()
                            .offeringType(e.getKey().name())
                            .totalAmount(typeTotal)
                            .serviceCount(e.getValue().size())
                            .percentage(pct)
                            .build();
                })
                .sorted(Comparator.comparing(OfferingReportResponse.OfferingTypeSummary::getTotalAmount).reversed())
                .toList();

        // By month
        Map<YearMonth, List<Offering>> byMonth = offerings.stream()
                .collect(Collectors.groupingBy(o -> YearMonth.from(o.getServiceDate())));

        List<OfferingReportResponse.MonthlyOfferingSummary> monthlySummaries = byMonth.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> OfferingReportResponse.MonthlyOfferingSummary.builder()
                        .year(e.getKey().getYear())
                        .month(e.getKey().getMonthValue())
                        .monthName(e.getKey().getMonth().name())
                        .totalAmount(sum(e.getValue(), Offering::getTotalAmount))
                        .serviceCount(e.getValue().size())
                        .cashAmount(sum(e.getValue(), Offering::getCashAmount))
                        .checkAmount(sum(e.getValue(), Offering::getCheckAmount))
                        .digitalAmount(sum(e.getValue(), Offering::getDigitalAmount))
                        .build())
                .toList();

        // Individual service records
        List<OfferingReportResponse.OfferingServiceRecord> serviceRecords = offerings.stream()
                .map(o -> OfferingReportResponse.OfferingServiceRecord.builder()
                        .serviceDate(o.getServiceDate())
                        .serviceDescription(o.getServiceDescription())
                        .offeringType(o.getOfferingType().name())
                        .totalAmount(o.getTotalAmount())
                        .cashAmount(o.getCashAmount())
                        .checkAmount(o.getCheckAmount())
                        .digitalAmount(o.getDigitalAmount())
                        .giverCount(o.getGiverCount())
                        .countedBy(o.getCountedBy())
                        .verifiedBy(o.getVerifiedBy())
                        .verified(o.isVerified())
                        .build())
                .toList();

        log.info("Offering report generated: {} to {} — {} services, total=${}",
                from, to, offerings.size(), grandTotal);

        return OfferingReportResponse.builder()
                .from(from).to(to)
                .generatedDate(LocalDate.now())
                .grandTotal(grandTotal)
                .totalCash(totalCash)
                .totalCheck(totalCheck)
                .totalDigital(totalDigital)
                .totalServices(offerings.size())
                .totalGivers(totalGivers)
                .averagePerService(avgPerService)
                .byType(typeSummaries)
                .byMonth(monthlySummaries)
                .services(serviceRecords)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DONATION REPORT
    // ══════════════════════════════════════════════════════════════════════════

    public DonationReportResponse generateDonationReport(LocalDate from, LocalDate to) {
        List<Donation> donations = donationRepository
                .findByPaymentDateBetweenOrderByPaymentDateDesc(from, to, Pageable.unpaged())
                .getContent();

        BigDecimal grandTotal = sum(donations, Donation::getAmount);
        long anonymous = donations.stream().filter(d -> Boolean.TRUE.equals(d.getIsAnonymous())).count();
        long memberLinked = donations.stream().filter(d -> d.getMembershipNumber() != null).count();
        long pendingReceipts = donations.stream()
                .filter(d -> !Boolean.TRUE.equals(d.getReceiptIssued())
                        && d.getMembershipNumber() != null).count();

        // By category
        Map<DonationCategory, List<Donation>> byCategory = donations.stream()
                .collect(Collectors.groupingBy(Donation::getCategory));

        List<DonationReportResponse.CategorySummary> categorySummaries = byCategory.entrySet().stream()
                .map(e -> {
                    BigDecimal catTotal = sum(e.getValue(), Donation::getAmount);
                    BigDecimal pct = grandTotal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                            : catTotal.divide(grandTotal, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                    // Most common fund name in this category
                    String fundName = e.getValue().stream()
                            .map(Donation::getFundName).filter(Objects::nonNull)
                            .findFirst().orElse(null);
                    return DonationReportResponse.CategorySummary.builder()
                            .category(e.getKey().name())
                            .fundName(fundName)
                            .totalAmount(catTotal)
                            .donationCount(e.getValue().size())
                            .percentage(pct)
                            .build();
                })
                .sorted(Comparator.comparing(DonationReportResponse.CategorySummary::getTotalAmount).reversed())
                .toList();

        // By payment method
        Map<PaymentMethod, List<Donation>> byMethod = donations.stream()
                .collect(Collectors.groupingBy(Donation::getPaymentMethod));

        List<DonationReportResponse.PaymentMethodSummary> methodSummaries = byMethod.entrySet().stream()
                .map(e -> DonationReportResponse.PaymentMethodSummary.builder()
                        .paymentMethod(e.getKey().name())
                        .totalAmount(sum(e.getValue(), Donation::getAmount))
                        .donationCount(e.getValue().size())
                        .build())
                .sorted(Comparator.comparing(DonationReportResponse.PaymentMethodSummary::getTotalAmount).reversed())
                .toList();

        // Top donors (non-anonymous, member-linked)
        Map<String, List<Donation>> byMember = donations.stream()
                .filter(d -> !Boolean.TRUE.equals(d.getIsAnonymous()) && d.getMembershipNumber() != null)
                .collect(Collectors.groupingBy(Donation::getMembershipNumber));

        List<DonationReportResponse.DonorSummary> topDonors = byMember.entrySet().stream()
                .map(e -> {
                    String donorName = e.getValue().get(0).getDonorName();
                    return DonationReportResponse.DonorSummary.builder()
                            .membershipNumber(e.getKey())
                            .donorName(donorName)
                            .totalDonated(sum(e.getValue(), Donation::getAmount))
                            .donationCount(e.getValue().size())
                            .build();
                })
                .sorted(Comparator.comparing(DonationReportResponse.DonorSummary::getTotalDonated).reversed())
                .limit(10)
                .toList();

        log.info("Donation report generated: {} to {} — {} donations, total=${}",
                from, to, donations.size(), grandTotal);

        return DonationReportResponse.builder()
                .from(from).to(to)
                .generatedDate(LocalDate.now())
                .grandTotal(grandTotal)
                .totalDonations(donations.size())
                .anonymousDonations((int) anonymous)
                .memberDonations((int) memberLinked)
                .pendingReceipts((int) pendingReceipts)
                .byCategory(categorySummaries)
                .byPaymentMethod(methodSummaries)
                .topDonors(topDonors)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // EXPENSE REPORT
    // ══════════════════════════════════════════════════════════════════════════

    // ══════════════════════════════════════════════════════════════════════════
    // EXPENSE REPORT  (corrected: byCategory + topVendors use APPROVED + PAID)
    // ══════════════════════════════════════════════════════════════════════════

    public ExpenseReportResponse generateExpenseReport(LocalDate from, LocalDate to) {
        List<Expense> expenses = expenseRepository
                .findByExpenseDateBetweenOrderByExpenseDateDesc(from, to, Pageable.unpaged())
                .getContent();

        BigDecimal totalPaid = sumExpensesByStatus(expenses, ExpenseStatus.PAID);
        BigDecimal totalApproved = sumExpensesByStatus(expenses, ExpenseStatus.APPROVED);
        BigDecimal totalPending = sumExpensesByStatus(expenses, ExpenseStatus.SUBMITTED);
        BigDecimal totalRejected = sumExpensesByStatus(expenses, ExpenseStatus.REJECTED);
        BigDecimal totalCancelled = sumExpensesByStatus(expenses, ExpenseStatus.CANCELLED);

        // By category — committed spend = APPROVED + PAID
        // (approved funds are reserved, so they belong in the breakdown alongside paid)
        List<ExpenseStatus> committedStatuses = List.of(ExpenseStatus.APPROVED, ExpenseStatus.PAID);
        List<Expense> committedExpenses = expenses.stream()
                .filter(e -> committedStatuses.contains(e.getStatus())).toList();
        BigDecimal committedTotal = sumE(committedExpenses);

        Map<ExpenseCategory, List<Expense>> byCategory = committedExpenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory));

        List<ExpenseReportResponse.CategoryBreakdown> categoryBreakdowns = byCategory.entrySet().stream()
                .map(e -> {
                    BigDecimal catTotal = sumE(e.getValue());
                    BigDecimal pct = committedTotal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                            : catTotal.divide(committedTotal, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                    return ExpenseReportResponse.CategoryBreakdown.builder()
                            .category(e.getKey().name())
                            .totalPaid(catTotal)
                            .count(e.getValue().size())
                            .percentage(pct)
                            .build();
                })
                .sorted(Comparator.comparing(ExpenseReportResponse.CategoryBreakdown::getTotalPaid).reversed())
                .toList();

        // Top vendors — also from committed spend
        Map<String, List<Expense>> byVendor = committedExpenses.stream()
                .filter(e -> e.getVendorName() != null)
                .collect(Collectors.groupingBy(Expense::getVendorName));

        List<ExpenseReportResponse.VendorSummary> topVendors = byVendor.entrySet().stream()
                .map(e -> ExpenseReportResponse.VendorSummary.builder()
                        .vendorName(e.getKey())
                        .totalAmount(sumE(e.getValue()))
                        .expenseCount(e.getValue().size())
                        .build())
                .sorted(Comparator.comparing(ExpenseReportResponse.VendorSummary::getTotalAmount).reversed())
                .limit(10)
                .toList();

        // Overdue
        List<ExpenseStatus> nonTerminal = List.of(
                ExpenseStatus.DRAFT, ExpenseStatus.SUBMITTED, ExpenseStatus.APPROVED);
        List<ExpenseReportResponse.OverdueExpenseItem> overdueItems = expenses.stream()
                .filter(e -> e.getDueDate() != null
                        && LocalDate.now().isAfter(e.getDueDate())
                        && nonTerminal.contains(e.getStatus()))
                .map(e -> ExpenseReportResponse.OverdueExpenseItem.builder()
                        .expenseId(e.getId().toString())
                        .title(e.getTitle())
                        .amount(e.getAmount())
                        .dueDate(e.getDueDate())
                        .status(e.getStatus().name())
                        .daysOverdue(ChronoUnit.DAYS.between(e.getDueDate(), LocalDate.now()))
                        .build())
                .sorted(Comparator.comparing(ExpenseReportResponse.OverdueExpenseItem::getDaysOverdue).reversed())
                .toList();

        int pendingCount = (int) expenses.stream()
                .filter(e -> ExpenseStatus.SUBMITTED.equals(e.getStatus())).count();
        int approvedCount = (int) expenses.stream()
                .filter(e -> ExpenseStatus.APPROVED.equals(e.getStatus())).count();
        int paidCount = (int) expenses.stream()
                .filter(e -> ExpenseStatus.PAID.equals(e.getStatus())).count();

        log.info("Expense report generated: {} to {} — {} expenses, paid=${} committed=${}",
                from, to, expenses.size(), totalPaid, committedTotal);

        return ExpenseReportResponse.builder()
                .from(from).to(to)
                .generatedDate(LocalDate.now())
                .totalPaid(totalPaid)
                .totalApproved(totalApproved)
                .totalPending(totalPending)
                .totalRejected(totalRejected)
                .totalCancelled(totalCancelled)
                .grandTotal(totalPaid.add(totalApproved).add(totalPending))
                .countPaid(paidCount)
                .countApproved(approvedCount)
                .countPending(pendingCount)
                .countOverdue(overdueItems.size())
                .byCategory(categoryBreakdowns)
                .topVendors(topVendors)
                .overdueExpenses(overdueItems)
                .build();
    }
    // ══════════════════════════════════════════════════════════════════════════
    // FINANCIAL OVERVIEW (Dashboard)
    // ══════════════════════════════════════════════════════════════════════════

    public FinancialOverviewReportResponse generateOverviewReport(LocalDate from, LocalDate to) {
        // Income sources
        List<Tithe> tithes = titheRepository.findByPaymentDateBetween(from, to);
        List<Donation> donations = donationRepository
                .findByPaymentDateBetweenOrderByPaymentDateDesc(from, to, Pageable.unpaged())
                .getContent();
        List<Offering> offerings = offeringRepository
                .findByServiceDateBetweenOrderByServiceDateDesc(from, to, Pageable.unpaged())
                .getContent();
        List<Expense> expenses = expenseRepository
                .findByExpenseDateBetweenOrderByExpenseDateDesc(from, to, Pageable.unpaged())
                .getContent();

        BigDecimal totalTithes    = sumT(tithes);
        BigDecimal totalDonations = sum(donations, Donation::getAmount);
        BigDecimal totalOfferings = sum(offerings, Offering::getTotalAmount);
        BigDecimal totalIncome    = totalTithes.add(totalDonations).add(totalOfferings);

        BigDecimal totalPaid     = sumExpensesByStatus(expenses, ExpenseStatus.PAID);
        BigDecimal totalApproved = sumExpensesByStatus(expenses, ExpenseStatus.APPROVED);
        BigDecimal totalPending  = sumExpensesByStatus(expenses, ExpenseStatus.SUBMITTED);

        BigDecimal availableBalance = totalIncome.subtract(totalPaid).subtract(totalApproved);
        BigDecimal netPosition      = totalIncome.subtract(totalPaid);

        long tithingMembers = tithes.stream()
                .map(Tithe::getMembershipNumber).distinct().count();

        // Monthly trend
        List<FinancialOverviewReportResponse.MonthlyTrendItem> trend = buildMonthlyTrend(
                from, to, tithes, donations, offerings, expenses);

        // Expense breakdown by category
        Map<ExpenseCategory, List<Expense>> byCategory = expenses.stream()
                .filter(e -> ExpenseStatus.PAID.equals(e.getStatus()))
                .collect(Collectors.groupingBy(Expense::getCategory));

        List<FinancialOverviewReportResponse.ExpenseCategorySummary> expenseCategories =
                byCategory.entrySet().stream()
                        .map(e -> {
                            BigDecimal catTotal = sumE(e.getValue());
                            BigDecimal pct = totalPaid.compareTo(BigDecimal.ZERO) == 0
                                    ? BigDecimal.ZERO
                                    : catTotal.divide(totalPaid, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(2, RoundingMode.HALF_UP);
                            return FinancialOverviewReportResponse.ExpenseCategorySummary.builder()
                                    .category(e.getKey().name())
                                    .totalAmount(catTotal)
                                    .count(e.getValue().size())
                                    .percentage(pct)
                                    .build();
                        })
                        .sorted(Comparator.comparing(
                                FinancialOverviewReportResponse.ExpenseCategorySummary::getTotalAmount).reversed())
                        .toList();

        log.info("Overview report generated: {} to {} — income=${} expenses=${}",
                from, to, totalIncome, totalPaid);

        return FinancialOverviewReportResponse.builder()
                .from(from).to(to)
                .generatedDate(LocalDate.now())
                .totalTithes(totalTithes)
                .totalDonations(totalDonations)
                .totalOfferings(totalOfferings)
                .totalIncome(totalIncome)
                .totalExpensesPaid(totalPaid)
                .totalExpensesApproved(totalApproved)
                .totalExpensesPending(totalPending)
                .availableBalance(availableBalance)
                .netPosition(netPosition)
                .titheCount((long) tithes.size())
                .donationCount((long) donations.size())
                .offeringCount((long) offerings.size())
                .expenseCount((long) expenses.size())
                .tithingMemberCount(tithingMembers)
                .monthlyTrend(trend)
                .expensesByCategory(expenseCategories)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private List<FinancialOverviewReportResponse.MonthlyTrendItem> buildMonthlyTrend(
            LocalDate from, LocalDate to,
            List<Tithe> tithes, List<Donation> donations,
            List<Offering> offerings, List<Expense> expenses) {

        List<FinancialOverviewReportResponse.MonthlyTrendItem> result = new ArrayList<>();
        YearMonth current = YearMonth.from(from);
        YearMonth end     = YearMonth.from(to);

        while (!current.isAfter(end)) {
            final YearMonth ym = current;

            BigDecimal monthTithes = tithes.stream()
                    .filter(t -> YearMonth.from(t.getPaymentDate()).equals(ym))
                    .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal monthDonations = donations.stream()
                    .filter(d -> YearMonth.from(d.getPaymentDate()).equals(ym))
                    .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal monthOfferings = offerings.stream()
                    .filter(o -> YearMonth.from(o.getServiceDate()).equals(ym))
                    .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal monthExpenses = expenses.stream()
                    .filter(e -> ExpenseStatus.PAID.equals(e.getStatus())
                            && YearMonth.from(e.getExpenseDate()).equals(ym))
                    .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal monthIncome = monthTithes.add(monthDonations).add(monthOfferings);

            result.add(FinancialOverviewReportResponse.MonthlyTrendItem.builder()
                    .year(ym.getYear())
                    .month(ym.getMonthValue())
                    .monthName(ym.getMonth().name())
                    .tithes(monthTithes)
                    .donations(monthDonations)
                    .offerings(monthOfferings)
                    .totalIncome(monthIncome)
                    .expensesPaid(monthExpenses)
                    .netPosition(monthIncome.subtract(monthExpenses))
                    .build());

            current = current.plusMonths(1);
        }
        return result;
    }


    // ══════════════════════════════════════════════════════════════════════════
    // CHURCH-WIDE TITHE REPORT
    // Add this method to FinanceReportService.java
    // ══════════════════════════════════════════════════════════════════════════

    public TitheReportResponse generateTitheReport(LocalDate from, LocalDate to) {
        List<Tithe> tithes = titheRepository.findByPaymentDateBetween(from, to);

        BigDecimal grandTotal = tithes.stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long tithingMembers = tithes.stream()
                .map(Tithe::getMembershipNumber)
                .distinct()
                .count();

        // By month
        Map<YearMonth, List<Tithe>> byMonth = tithes.stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getPaymentDate())));

        List<TitheReportResponse.MonthlyTitheSummary> monthlySummaries = byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    BigDecimal monthTotal = e.getValue().stream()
                            .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal pct = grandTotal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                            : monthTotal.divide(grandTotal, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                    return TitheReportResponse.MonthlyTitheSummary.builder()
                            .year(e.getKey().getYear())
                            .month(e.getKey().getMonthValue())
                            .monthName(e.getKey().getMonth().name())
                            .totalAmount(monthTotal)
                            .titheCount(e.getValue().size())
                            .percentage(pct)
                            .build();
                })
                .toList();

        // By member (top tithers)
        Map<String, List<Tithe>> byMember = tithes.stream()
                .collect(Collectors.groupingBy(Tithe::getMembershipNumber));

        List<TitheReportResponse.MemberTitheSummary> topTithers = byMember.entrySet().stream()
                .map(e -> {
                    BigDecimal memberTotal = e.getValue().stream()
                            .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    String memberName = e.getValue().stream()
                            .map(Tithe::getMemberName)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(e.getKey());
                    return TitheReportResponse.MemberTitheSummary.builder()
                            .membershipNumber(e.getKey())
                            .memberName(memberName)
                            .totalAmount(memberTotal)
                            .paymentCount(e.getValue().size())
                            .build();
                })
                .sorted(Comparator.comparing(TitheReportResponse.MemberTitheSummary::getTotalAmount).reversed())
                .toList();

        // By payment method
        Map<PaymentMethod, List<Tithe>> byMethod = tithes.stream()
                .collect(Collectors.groupingBy(Tithe::getPaymentMethod));

        List<TitheReportResponse.PaymentMethodSummary> methodSummaries = byMethod.entrySet().stream()
                .map(e -> {
                    BigDecimal methodTotal = e.getValue().stream()
                            .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return TitheReportResponse.PaymentMethodSummary.builder()
                            .paymentMethod(e.getKey().name())
                            .totalAmount(methodTotal)
                            .titheCount(e.getValue().size())
                            .build();
                })
                .sorted(Comparator.comparing(TitheReportResponse.PaymentMethodSummary::getTotalAmount).reversed())
                .toList();

        log.info("Tithe report generated: {} to {} — {} tithes, {} members, total=${}",
                from, to, tithes.size(), tithingMembers, grandTotal);

        return TitheReportResponse.builder()
                .from(from)
                .to(to)
                .generatedDate(LocalDate.now())
                .grandTotal(grandTotal)
                .totalTithes(tithes.size())
                .tithingMemberCount(tithingMembers)
                .byMonth(monthlySummaries)
                .topTithers(topTithers)
                .byPaymentMethod(methodSummaries)
                .build();
    }



    // Generic sum helpers to avoid repetition
    private <T> BigDecimal sum(List<T> list, java.util.function.Function<T, BigDecimal> extractor) {
        return list.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumT(List<Tithe> list) {
        return list.stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumE(List<Expense> list) {
        return list.stream()
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumExpensesByStatus(List<Expense> expenses, ExpenseStatus status) {
        return expenses.stream()
                .filter(e -> status.equals(e.getStatus()))
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


}