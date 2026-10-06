package com.churchflow360.attendanceservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class QrTokenExpiredException extends RuntimeException {
    public QrTokenExpiredException(String message) { super(message); }
}