package com.churchflow360.attendanceservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a new service event overlaps in time with an existing event
 * at the same location. A location can only host one event at a time.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class LocationConflictException extends RuntimeException {
    public LocationConflictException(String message) { super(message); }
}