package com.churchflow360.financeservice.dto.request;

import com.churchflow360.financeservice.model.ExpenseCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateExpenseRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 300)
    private String title;

    private String description;

    @NotNull(message = "Category is required")
    private ExpenseCategory category;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;

    private String currency;
    private String vendorName;
    private String vendorReference;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    private LocalDate dueDate;
    private Boolean isRecurring;
    private String recurrenceNote;
    private String receiptUrl;
    private String notes;

    @NotBlank(message = "Submitted by membership number is required")
    @Pattern(regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
            message = "submittedBy: invalid format. Expected MEM-MMDD-YYYY-HHMMSS")
    private String submittedBy;

    @NotBlank(message = "Recorded by membership number is required")
    @Pattern(regexp = "^MEM-\\d{4}-\\d{4}-\\d{6}$",
            message = "recordedBy: invalid format. Expected MEM-MMDD-YYYY-HHMMSS")
    private String recordedBy;
}