package com.churchflow360.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a business rule validation fails that cannot be expressed
 * as a Jakarta Bean Validation constraint.
 * Examples: password mismatch, self-role-change attempt, same password reuse.
 * Returns HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {
    public ValidationException(String message) { super(message); }
}