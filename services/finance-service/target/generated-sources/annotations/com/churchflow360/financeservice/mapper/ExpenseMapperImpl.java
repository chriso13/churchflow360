package com.churchflow360.financeservice.mapper;

import com.churchflow360.financeservice.dto.request.CreateExpenseRequest;
import com.churchflow360.financeservice.dto.response.ExpenseResponse;
import com.churchflow360.financeservice.model.Expense;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-28T22:51:57-0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class ExpenseMapperImpl implements ExpenseMapper {

    @Override
    public Expense toEntity(CreateExpenseRequest req) {
        if ( req == null ) {
            return null;
        }

        Expense.ExpenseBuilder expense = Expense.builder();

        expense.title( req.getTitle() );
        expense.description( req.getDescription() );
        expense.category( req.getCategory() );
        expense.amount( req.getAmount() );
        expense.vendorName( req.getVendorName() );
        expense.vendorReference( req.getVendorReference() );
        expense.expenseDate( req.getExpenseDate() );
        expense.dueDate( req.getDueDate() );
        expense.submittedBy( req.getSubmittedBy() );
        expense.recurrenceNote( req.getRecurrenceNote() );
        expense.receiptUrl( req.getReceiptUrl() );
        expense.notes( req.getNotes() );
        expense.recordedBy( req.getRecordedBy() );

        expense.status( com.churchflow360.financeservice.model.ExpenseStatus.DRAFT );
        expense.currency( req.getCurrency() != null ? req.getCurrency() : "USD" );
        expense.isRecurring( req.getIsRecurring() != null ? req.getIsRecurring() : Boolean.FALSE );

        return expense.build();
    }

    @Override
    public ExpenseResponse toResponse(Expense expense) {
        if ( expense == null ) {
            return null;
        }

        ExpenseResponse expenseResponse = new ExpenseResponse();

        expenseResponse.setId( expense.getId() );
        expenseResponse.setTitle( expense.getTitle() );
        expenseResponse.setDescription( expense.getDescription() );
        expenseResponse.setCategory( expense.getCategory() );
        expenseResponse.setAmount( expense.getAmount() );
        expenseResponse.setCurrency( expense.getCurrency() );
        expenseResponse.setVendorName( expense.getVendorName() );
        expenseResponse.setVendorReference( expense.getVendorReference() );
        expenseResponse.setExpenseDate( expense.getExpenseDate() );
        expenseResponse.setDueDate( expense.getDueDate() );
        expenseResponse.setPaidDate( expense.getPaidDate() );
        expenseResponse.setPaymentMethod( expense.getPaymentMethod() );
        expenseResponse.setPaymentReference( expense.getPaymentReference() );
        expenseResponse.setStatus( expense.getStatus() );
        expenseResponse.setSubmittedBy( expense.getSubmittedBy() );
        expenseResponse.setSubmittedAt( expense.getSubmittedAt() );
        expenseResponse.setApprovedBy( expense.getApprovedBy() );
        expenseResponse.setApprovedAt( expense.getApprovedAt() );
        expenseResponse.setRejectionReason( expense.getRejectionReason() );
        expenseResponse.setIsRecurring( expense.getIsRecurring() );
        expenseResponse.setRecurrenceNote( expense.getRecurrenceNote() );
        expenseResponse.setReceiptUrl( expense.getReceiptUrl() );
        expenseResponse.setNotes( expense.getNotes() );
        expenseResponse.setRecordedBy( expense.getRecordedBy() );
        expenseResponse.setCreatedAt( expense.getCreatedAt() );
        expenseResponse.setUpdatedAt( expense.getUpdatedAt() );

        expenseResponse.setOverdue( expense.isOverdue() );

        return expenseResponse;
    }
}
