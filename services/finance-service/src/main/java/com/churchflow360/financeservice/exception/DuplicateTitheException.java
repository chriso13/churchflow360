package com.churchflow360.financeservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateTitheException extends RuntimeException {
    public DuplicateTitheException(String message) { super(message); }
}