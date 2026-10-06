package com.churchflow360.pastoralservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MeetingOperationException extends RuntimeException {
    public MeetingOperationException(String message) { super(message); }
}