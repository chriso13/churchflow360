package com.churchflow360.financeservice.service;

import com.churchflow360.financeservice.client.MemberClient;
import com.churchflow360.financeservice.dto.request.*;
import com.churchflow360.financeservice.dto.response.*;
import com.churchflow360.financeservice.exception.ResourceNotFoundException;
import com.churchflow360.financeservice.mapper.FinanceMapper;
import com.churchflow360.financeservice.model.*;
import com.churchflow360.financeservice.repository.*;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FinanceService {

    private final TitheRepository        titheRepository;
    private final DonationRepository     donationRepository;
    private final OfferingRepository     offeringRepository;
    private final OfferingItemRepository offeringItemRepository;
    private final FinanceMapper          financeMapper;
    private final MemberClient           memberClient;

    // ══════════════════════════════════════════════════════════════════════════
    // TITHES
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional
    public TitheResponse recordTithe(RecordTitheRequest request) {
        memberClient.verifyMemberExists(request.getMembershipNumber());

        // No duplicate check — a member can record multiple tithes per month
        Tithe tithe = financeMapper.toEntity(request);
        tithe.setMembershipNumber(request.getMembershipNumber().toUpperCase());
        Tithe saved = titheRepository.save(tithe);
        log.info("Tithe recorded: {} {} {} — ${}",
                saved.getMembershipNumber(),
                saved.getTitheMonth(), saved.getTitheYear(), saved.getAmount());
        return financeMapper.toResponse(saved);
    }

    public TitheResponse getTithe(UUID id) {
        return financeMapper.toResponse(findTitheOrThrow(id));
    }

    public PagedResponse<TitheResponse> getMemberTithes(
            String membershipNumber, Pageable pageable) {
        memberClient.verifyMemberExists(membershipNumber);
        return PagedResponse.of(
                titheRepository.findByMembershipNumberOrderByPaymentDateDesc(
                                membershipNumber.toUpperCase(), pageable)
                        .map(financeMapper::toResponse));
    }

    public PagedResponse<TitheResponse> getTithesByDateRange(
            LocalDate from, LocalDate to, Pageable pageable) {
        return PagedResponse.of(
                titheRepository.findByPaymentDateBetweenOrderByPaymentDateDesc(from, to, pageable)
                        .map(financeMapper::toResponse));
    }

    public BigDecimal getMemberTitheTotal(String membershipNumber, Integer year) {
        memberClient.verifyMemberExists(membershipNumber);
        // Sum in Java — no @Query needed
        return titheRepository.findByMembershipNumber(membershipNumber.toUpperCase())
                .stream()
                .filter(t -> t.getTitheYear().equals(year))
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DONATIONS
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional
    public DonationResponse recordDonation(RecordDonationRequest request) {
        if (request.getMembershipNumber() != null && !request.getMembershipNumber().isBlank()) {
            memberClient.verifyMemberExists(request.getMembershipNumber());
        }

        Donation donation = financeMapper.toEntity(request);
        if (donation.getMembershipNumber() != null) {
            donation.setMembershipNumber(donation.getMembershipNumber().toUpperCase());
        }

        Donation saved = donationRepository.save(donation);
        log.info("Donation recorded: {} {} — ${}",
                saved.getDisplayDonorName(), saved.getCategory(), saved.getAmount());
        return financeMapper.toResponse(saved);
    }

    public DonationResponse getDonation(UUID id) {
        return financeMapper.toResponse(findDonationOrThrow(id));
    }

    public PagedResponse<DonationResponse> getMemberDonations(
            String membershipNumber, Pageable pageable) {
        memberClient.verifyMemberExists(membershipNumber);
        return PagedResponse.of(
                donationRepository.findByMembershipNumberOrderByPaymentDateDesc(
                                membershipNumber.toUpperCase(), pageable)
                        .map(financeMapper::toResponse));
    }

    public PagedResponse<DonationResponse> getDonationsByCategory(
            DonationCategory category, Pageable pageable) {
        return PagedResponse.of(
                donationRepository.findByCategoryOrderByPaymentDateDesc(category, pageable)
                        .map(financeMapper::toResponse));
    }

    public PagedResponse<DonationResponse> getDonationsByDateRange(
            LocalDate from, LocalDate to, Pageable pageable) {
        return PagedResponse.of(
                donationRepository.findByPaymentDateBetweenOrderByPaymentDateDesc(from, to, pageable)
                        .map(financeMapper::toResponse));
    }

    @Transactional
    public DonationResponse issueReceipt(UUID donationId) {
        Donation donation = findDonationOrThrow(donationId);
        if (Boolean.TRUE.equals(donation.getReceiptIssued())) {
            return financeMapper.toResponse(donation);
        }
        donation.setReceiptIssued(true);
        donation.setReceiptIssuedAt(LocalDateTime.now());
        donation.setReceiptNumber("RCP-" + donationId.toString().substring(0, 8).toUpperCase());
        return financeMapper.toResponse(donationRepository.save(donation));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // OFFERINGS
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional
    public OfferingResponse recordOffering(RecordOfferingRequest request) {
        Offering offering = financeMapper.toEntity(request);
        Offering saved = offeringRepository.save(offering);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (AddOfferingItemRequest itemReq : request.getItems()) {
                OfferingItem item = financeMapper.toEntity(itemReq);
                item.setOffering(saved);
                offeringItemRepository.save(item);
            }
        }

        log.info("Offering recorded: {} {} — ${}",
                saved.getOfferingType(), saved.getServiceDate(), saved.getTotalAmount());
        return buildOfferingResponse(saved);
    }

    public OfferingResponse getOffering(UUID id) {
        return buildOfferingResponse(findOfferingOrThrow(id));
    }

    public List<OfferingResponse> getOfferingsByDate(LocalDate date) {
        return offeringRepository.findByServiceDateOrderByOfferingTypeAsc(date)
                .stream().map(this::buildOfferingResponse).toList();
    }

    public PagedResponse<OfferingResponse> getOfferingsByDateRange(
            LocalDate from, LocalDate to, Pageable pageable) {
        return PagedResponse.of(
                offeringRepository.findByServiceDateBetweenOrderByServiceDateDesc(from, to, pageable)
                        .map(this::buildOfferingResponse));
    }

    @Transactional
    public OfferingResponse addItemToOffering(UUID offeringId, AddOfferingItemRequest request) {
        Offering offering = findOfferingOrThrow(offeringId);

        if (request.getMembershipNumber() != null && !request.getMembershipNumber().isBlank()) {
            memberClient.verifyMemberExists(request.getMembershipNumber());
        }

        OfferingItem item = financeMapper.toEntity(request);
        item.setOffering(offering);
        offeringItemRepository.save(item);

        return buildOfferingResponse(offeringRepository.findById(offeringId).orElseThrow());
    }

    @Transactional
    public OfferingResponse verifyOffering(UUID id, String verifiedBy) {
        Offering offering = findOfferingOrThrow(id);
        offering.setVerifiedBy(verifiedBy);
        offering.setVerifiedAt(LocalDateTime.now());
        return buildOfferingResponse(offeringRepository.save(offering));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SUMMARY
    // ══════════════════════════════════════════════════════════════════════════

    public FinanceSummaryResponse getSummary(LocalDate from, LocalDate to) {
        // Sum tithes in date range in Java
        BigDecimal totalTithes = titheRepository.findByPaymentDateBetween(from, to)
                .stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sum donations in date range in Java
        BigDecimal totalDonations = donationRepository
                .findByPaymentDateBetweenOrderByPaymentDateDesc(from, to, Pageable.unpaged())
                .stream()
                .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sum offerings in date range in Java
        BigDecimal totalOfferings = offeringRepository
                .findByServiceDateBetweenOrderByServiceDateDesc(from, to, Pageable.unpaged())
                .stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count distinct tithing members in the year
        long tithingMembers = titheRepository.findByTitheYear(from.getYear())
                .stream()
                .map(Tithe::getMembershipNumber)
                .distinct()
                .count();

        return FinanceSummaryResponse.builder()
                .from(from).to(to)
                .totalTithes(totalTithes)
                .totalDonations(totalDonations)
                .totalOfferings(totalOfferings)
                .grandTotal(totalTithes.add(totalDonations).add(totalOfferings))
                .titheCount(titheRepository.findByPaymentDateBetween(from, to).size())
                .donationCount(donationRepository
                        .findByPaymentDateBetweenOrderByPaymentDateDesc(from, to, Pageable.unpaged())
                        .getTotalElements())
                .offeringCount(offeringRepository
                        .findByServiceDateBetweenOrderByServiceDateDesc(from, to, Pageable.unpaged())
                        .getTotalElements())
                .tithingMemberCount(tithingMembers)
                .build();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Tithe findTitheOrThrow(UUID id) {
        return titheRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tithe not found: " + id));
    }

    private Donation findDonationOrThrow(UUID id) {
        return donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + id));
    }

    private Offering findOfferingOrThrow(UUID id) {
        return offeringRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offering not found: " + id));
    }

    private OfferingResponse buildOfferingResponse(Offering offering) {
        OfferingResponse response = financeMapper.toResponse(offering);
        List<OfferingItemResponse> items = offeringItemRepository
                .findByOfferingIdOrderByCreatedAtAsc(offering.getId())
                .stream().map(financeMapper::toResponse).collect(Collectors.toList());
        response.setItems(items);
        return response;
    }
}