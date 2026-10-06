package com.churchflow360.financeservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when member-service is unreachable and member verification
 * cannot be completed. Write operations are rejected rather than
 * proceeding with unverified membership numbers.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class MemberServiceUnavailableException extends RuntimeException {
    public MemberServiceUnavailableException(String message) { super(message); }
}