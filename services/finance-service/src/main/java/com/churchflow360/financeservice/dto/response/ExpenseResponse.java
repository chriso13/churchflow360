package com.churchflow360.financeservice.dto.response;

import com.churchflow360.financeservice.model.ExpenseCategory;
import com.churchflow360.financeservice.model.ExpenseStatus;
import com.churchflow360.financeservice.model.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ExpenseResponse {
    private UUID id;
    private String title;
    private String description;
    private ExpenseCategory category;
    private BigDecimal amount;
    private String currency;
    private String vendorName;
    private String vendorReference;
    private LocalDate expenseDate;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private PaymentMethod paymentMethod;
    private String paymentReference;
    private ExpenseStatus status;
    private String submittedBy;
    private LocalDateTime submittedAt;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private Boolean isRecurring;
    private String recurrenceNote;
    private String receiptUrl;
    private boolean overdue;
    private String notes;
    private String recordedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}