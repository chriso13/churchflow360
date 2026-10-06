package com.churchflow360.financeservice.mapper;

import com.churchflow360.financeservice.dto.request.*;
import com.churchflow360.financeservice.dto.response.*;
import com.churchflow360.financeservice.model.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FinanceMapper {

    // ── Tithe ──────────────────────────────────────────────────────────────────

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "currency",  expression = "java(req.getCurrency() != null ? req.getCurrency() : \"USD\")")
    Tithe toEntity(RecordTitheRequest req);

    @Mapping(target = "periodLabel", expression = "java(tithe.getPeriodLabel())")
    TitheResponse toResponse(Tithe tithe);

    // ── Donation ───────────────────────────────────────────────────────────────

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "receiptNumber",   ignore = true)
    @Mapping(target = "receiptIssued",   constant = "false")
    @Mapping(target = "receiptIssuedAt", ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    @Mapping(target = "currency",        expression = "java(req.getCurrency() != null ? req.getCurrency() : \"USD\")")
    @Mapping(target = "isAnonymous",     expression = "java(req.getIsAnonymous() != null ? req.getIsAnonymous() : Boolean.FALSE)")
    @Mapping(target = "isRecurring",     expression = "java(req.getIsRecurring() != null ? req.getIsRecurring() : Boolean.FALSE)")
    Donation toEntity(RecordDonationRequest req);

    DonationResponse toResponse(Donation donation);

    // ── Offering ───────────────────────────────────────────────────────────────

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    @Mapping(target = "items",      ignore = true)
    @Mapping(target = "currency",   expression = "java(req.getCurrency() != null ? req.getCurrency() : \"USD\")")
    @Mapping(target = "cashAmount",    expression = "java(req.getCashAmount() != null ? req.getCashAmount() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "checkAmount",   expression = "java(req.getCheckAmount() != null ? req.getCheckAmount() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "digitalAmount", expression = "java(req.getDigitalAmount() != null ? req.getDigitalAmount() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "giverCount",    expression = "java(req.getGiverCount() != null ? req.getGiverCount() : 0)")
    Offering toEntity(RecordOfferingRequest req);

    @Mapping(target = "verified",    expression = "java(offering.isVerified())")
    @Mapping(target = "items",       ignore = true)
    OfferingResponse toResponse(Offering offering);

    // ── Offering Item ──────────────────────────────────────────────────────────

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "offering",  ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isAnonymous", expression = "java(req.getIsAnonymous() != null ? req.getIsAnonymous() : Boolean.FALSE)")
    OfferingItem toEntity(AddOfferingItemRequest req);

    OfferingItemResponse toResponse(OfferingItem item);
}