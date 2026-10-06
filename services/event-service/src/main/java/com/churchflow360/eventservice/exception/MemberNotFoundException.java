package com.churchflow360.eventservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(String message) { super(message); }
}