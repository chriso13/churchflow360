package com.churchflow360.financeservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a finance operation violates a business rule,
 * such as an invalid status transition or a self-approval attempt.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class FinanceOperationException extends RuntimeException {
    public FinanceOperationException(String message) { super(message); }
}