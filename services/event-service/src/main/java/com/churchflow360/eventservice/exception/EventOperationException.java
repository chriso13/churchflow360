package com.churchflow360.eventservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EventOperationException extends RuntimeException {
    public EventOperationException(String message) { super(message); }
}