package com.churchflow360.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown during registration when the provided membershipNumber
 * does not exist in member-service.
 * Returns HTTP 422 Unprocessable Entity.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class MemberVerificationException extends RuntimeException {
    public MemberVerificationException(String message) { super(message); }
}