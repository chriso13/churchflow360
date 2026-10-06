package com.churchflow360.financeservice.mapper;

import com.churchflow360.financeservice.dto.request.AddOfferingItemRequest;
import com.churchflow360.financeservice.dto.request.RecordDonationRequest;
import com.churchflow360.financeservice.dto.request.RecordOfferingRequest;
import com.churchflow360.financeservice.dto.request.RecordTitheRequest;
import com.churchflow360.financeservice.dto.response.DonationResponse;
import com.churchflow360.financeservice.dto.response.OfferingItemResponse;
import com.churchflow360.financeservice.dto.response.OfferingResponse;
import com.churchflow360.financeservice.dto.response.TitheResponse;
import com.churchflow360.financeservice.model.Donation;
import com.churchflow360.financeservice.model.Offering;
import com.churchflow360.financeservice.model.OfferingItem;
import com.churchflow360.financeservice.model.Tithe;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-28T22:51:57-0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class FinanceMapperImpl implements FinanceMapper {

    @Override
    public Tithe toEntity(RecordTitheRequest req) {
        if ( req == null ) {
            return null;
        }

        Tithe.TitheBuilder tithe = Tithe.builder();

        tithe.membershipNumber( req.getMembershipNumber() );
        tithe.memberName( req.getMemberName() );
        tithe.amount( req.getAmount() );
        tithe.paymentDate( req.getPaymentDate() );
        tithe.paymentMethod( req.getPaymentMethod() );
        tithe.referenceNumber( req.getReferenceNumber() );
        tithe.titheMonth( req.getTitheMonth() );
        tithe.titheYear( req.getTitheYear() );
        tithe.notes( req.getNotes() );
        tithe.recordedBy( req.getRecordedBy() );

        tithe.currency( req.getCurrency() != null ? req.getCurrency() : "USD" );

        return tithe.build();
    }

    @Override
    public TitheResponse toResponse(Tithe tithe) {
        if ( tithe == null ) {
            return null;
        }

        TitheResponse titheResponse = new TitheResponse();

        titheResponse.setId( tithe.getId() );
        titheResponse.setMembershipNumber( tithe.getMembershipNumber() );
        titheResponse.setMemberName( tithe.getMemberName() );
        titheResponse.setAmount( tithe.getAmount() );
        titheResponse.setCurrency( tithe.getCurrency() );
        titheResponse.setPaymentDate( tithe.getPaymentDate() );
        titheResponse.setPaymentMethod( tithe.getPaymentMethod() );
        titheResponse.setReferenceNumber( tithe.getReferenceNumber() );
        titheResponse.setTitheMonth( tithe.getTitheMonth() );
        titheResponse.setTitheYear( tithe.getTitheYear() );
        titheResponse.setNotes( tithe.getNotes() );
        titheResponse.setRecordedBy( tithe.getRecordedBy() );
        titheResponse.setCreatedAt( tithe.getCreatedAt() );

        titheResponse.setPeriodLabel( tithe.getPeriodLabel() );

        return titheResponse;
    }

    @Override
    public Donation toEntity(RecordDonationRequest req) {
        if ( req == null ) {
            return null;
        }

        Donation.DonationBuilder donation = Donation.builder();

        donation.membershipNumber( req.getMembershipNumber() );
        donation.donorName( req.getDonorName() );
        donation.donorEmail( req.getDonorEmail() );
        donation.amount( req.getAmount() );
        donation.category( req.getCategory() );
        donation.fundName( req.getFundName() );
        donation.paymentDate( req.getPaymentDate() );
        donation.paymentMethod( req.getPaymentMethod() );
        donation.referenceNumber( req.getReferenceNumber() );
        donation.notes( req.getNotes() );
        donation.recordedBy( req.getRecordedBy() );

        donation.receiptIssued( false );
        donation.currency( req.getCurrency() != null ? req.getCurrency() : "USD" );
        donation.isAnonymous( req.getIsAnonymous() != null ? req.getIsAnonymous() : Boolean.FALSE );
        donation.isRecurring( req.getIsRecurring() != null ? req.getIsRecurring() : Boolean.FALSE );

        return donation.build();
    }

    @Override
    public DonationResponse toResponse(Donation donation) {
        if ( donation == null ) {
            return null;
        }

        DonationResponse donationResponse = new DonationResponse();

        donationResponse.setId( donation.getId() );
        donationResponse.setMembershipNumber( donation.getMembershipNumber() );
        donationResponse.setDonorName( donation.getDonorName() );
        donationResponse.setDonorEmail( donation.getDonorEmail() );
        donationResponse.setAmount( donation.getAmount() );
        donationResponse.setCurrency( donation.getCurrency() );
        donationResponse.setCategory( donation.getCategory() );
        donationResponse.setFundName( donation.getFundName() );
        donationResponse.setIsAnonymous( donation.getIsAnonymous() );
        donationResponse.setIsRecurring( donation.getIsRecurring() );
        donationResponse.setPaymentDate( donation.getPaymentDate() );
        donationResponse.setPaymentMethod( donation.getPaymentMethod() );
        donationResponse.setReferenceNumber( donation.getReferenceNumber() );
        donationResponse.setReceiptNumber( donation.getReceiptNumber() );
        donationResponse.setReceiptIssued( donation.getReceiptIssued() );
        donationResponse.setReceiptIssuedAt( donation.getReceiptIssuedAt() );
        donationResponse.setNotes( donation.getNotes() );
        donationResponse.setRecordedBy( donation.getRecordedBy() );
        donationResponse.setCreatedAt( donation.getCreatedAt() );

        return donationResponse;
    }

    @Override
    public Offering toEntity(RecordOfferingRequest req) {
        if ( req == null ) {
            return null;
        }

        Offering.OfferingBuilder offering = Offering.builder();

        offering.serviceDate( req.getServiceDate() );
        offering.serviceDescription( req.getServiceDescription() );
        offering.offeringType( req.getOfferingType() );
        offering.totalAmount( req.getTotalAmount() );
        offering.countedBy( req.getCountedBy() );
        offering.notes( req.getNotes() );
        offering.recordedBy( req.getRecordedBy() );

        offering.currency( req.getCurrency() != null ? req.getCurrency() : "USD" );
        offering.cashAmount( req.getCashAmount() != null ? req.getCashAmount() : java.math.BigDecimal.ZERO );
        offering.checkAmount( req.getCheckAmount() != null ? req.getCheckAmount() : java.math.BigDecimal.ZERO );
        offering.digitalAmount( req.getDigitalAmount() != null ? req.getDigitalAmount() : java.math.BigDecimal.ZERO );
        offering.giverCount( req.getGiverCount() != null ? req.getGiverCount() : 0 );

        return offering.build();
    }

    @Override
    public OfferingResponse toResponse(Offering offering) {
        if ( offering == null ) {
            return null;
        }

        OfferingResponse offeringResponse = new OfferingResponse();

        offeringResponse.setId( offering.getId() );
        offeringResponse.setServiceDate( offering.getServiceDate() );
        offeringResponse.setServiceDescription( offering.getServiceDescription() );
        offeringResponse.setOfferingType( offering.getOfferingType() );
        offeringResponse.setTotalAmount( offering.getTotalAmount() );
        offeringResponse.setCurrency( offering.getCurrency() );
        offeringResponse.setCashAmount( offering.getCashAmount() );
        offeringResponse.setCheckAmount( offering.getCheckAmount() );
        offeringResponse.setDigitalAmount( offering.getDigitalAmount() );
        offeringResponse.setGiverCount( offering.getGiverCount() );
        offeringResponse.setCountedBy( offering.getCountedBy() );
        offeringResponse.setVerifiedBy( offering.getVerifiedBy() );
        offeringResponse.setVerifiedAt( offering.getVerifiedAt() );
        offeringResponse.setNotes( offering.getNotes() );
        offeringResponse.setRecordedBy( offering.getRecordedBy() );
        offeringResponse.setCreatedAt( offering.getCreatedAt() );

        offeringResponse.setVerified( offering.isVerified() );

        return offeringResponse;
    }

    @Override
    public OfferingItem toEntity(AddOfferingItemRequest req) {
        if ( req == null ) {
            return null;
        }

        OfferingItem.OfferingItemBuilder offeringItem = OfferingItem.builder();

        offeringItem.membershipNumber( req.getMembershipNumber() );
        offeringItem.giverName( req.getGiverName() );
        offeringItem.amount( req.getAmount() );
        offeringItem.paymentMethod( req.getPaymentMethod() );
        offeringItem.envelopeNumber( req.getEnvelopeNumber() );
        offeringItem.notes( req.getNotes() );

        offeringItem.isAnonymous( req.getIsAnonymous() != null ? req.getIsAnonymous() : Boolean.FALSE );

        return offeringItem.build();
    }

    @Override
    public OfferingItemResponse toResponse(OfferingItem item) {
        if ( item == null ) {
            return null;
        }

        OfferingItemResponse offeringItemResponse = new OfferingItemResponse();

        offeringItemResponse.setId( item.getId() );
        offeringItemResponse.setMembershipNumber( item.getMembershipNumber() );
        offeringItemResponse.setGiverName( item.getGiverName() );
        offeringItemResponse.setIsAnonymous( item.getIsAnonymous() );
        offeringItemResponse.setAmount( item.getAmount() );
        offeringItemResponse.setPaymentMethod( item.getPaymentMethod() );
        offeringItemResponse.setEnvelopeNumber( item.getEnvelopeNumber() );
        offeringItemResponse.setNotes( item.getNotes() );
        offeringItemResponse.setCreatedAt( item.getCreatedAt() );

        return offeringItemResponse;
    }
}
