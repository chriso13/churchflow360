package com.churchflow360.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when member-service is unreachable during registration
 * and the membership number cannot be verified.
 * Returns HTTP 503 Service Unavailable.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class MemberServiceUnavailableException extends RuntimeException {
    public MemberServiceUnavailableException(String message) { super(message); }
}