package com.churchflow360.notification.exception;
import com.churchflow360.notification.dto.response.ApiResponse;
import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*; import java.util.*;
import java.util.stream.Collectors;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String,String>>> validation(MethodArgumentNotValidException e) {
        Map<String,String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(f -> f.getField(), f -> f.getDefaultMessage(), (a,b)->b));
        return ResponseEntity.badRequest().body(ApiResponse.<Map<String,String>>builder().success(false).message("Validation failed").data(errors).build());
    }
}
