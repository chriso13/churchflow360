package com.churchflow360.financeservice.mapper;

import com.churchflow360.financeservice.dto.request.CreateExpenseRequest;
import com.churchflow360.financeservice.dto.response.ExpenseResponse;
import com.churchflow360.financeservice.model.Expense;
import com.churchflow360.financeservice.model.ExpenseStatus;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExpenseMapper {

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "status",          expression = "java(com.churchflow360.financeservice.model.ExpenseStatus.DRAFT)")
    @Mapping(target = "submittedAt",     ignore = true)
    @Mapping(target = "approvedBy",      ignore = true)
    @Mapping(target = "approvedAt",      ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "paymentMethod",   ignore = true)
    @Mapping(target = "paymentReference",ignore = true)
    @Mapping(target = "paidDate",        ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    @Mapping(target = "currency",        expression = "java(req.getCurrency() != null ? req.getCurrency() : \"USD\")")
    @Mapping(target = "isRecurring",     expression = "java(req.getIsRecurring() != null ? req.getIsRecurring() : Boolean.FALSE)")
    Expense toEntity(CreateExpenseRequest req);

    @Mapping(target = "overdue", expression = "java(expense.isOverdue())")
    ExpenseResponse toResponse(Expense expense);
}