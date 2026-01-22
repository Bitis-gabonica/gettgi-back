package com.gettgi.mvp.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        if (log.isDebugEnabled()) {
            log.debug("Application error: {}", ex.getMessage(), ex);
        }
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of(
                        "status", ex.getStatusCode().value(),
                        "error", ex.getReason() != null ? ex.getReason() : "error",
                        "message", ex.getReason()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Resource conflict";
        HttpStatus status;
        if (message.contains("introuvable") || message.toLowerCase().contains("not found")) {
            status = HttpStatus.NOT_FOUND;
        } else if (message.toLowerCase().contains("non autoris") || message.toLowerCase().contains("forbidden")) {
            status = HttpStatus.FORBIDDEN;
        } else if (message.toLowerCase().contains("existe deja") || message.toLowerCase().contains("already")) {
            status = HttpStatus.CONFLICT;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        if (status.is5xxServerError()) {
            log.error("Unexpected IllegalStateException", ex);
        } else if (log.isDebugEnabled()) {
            log.debug("Business exception: {}", ex.getMessage());
        }

        return ResponseEntity.status(status)
                .body(Map.of(
                        "status", status.value(),
                        "error", status.getReasonPhrase(),
                        "message", message
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        if (log.isDebugEnabled()) {
            log.debug("Validation error: {}", ex.getMessage());
        }

        List<Map<String, Object>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("field", error.getField());
                    errorMap.put("rejectedValue", error.getRejectedValue());
                    errorMap.put("message", error.getDefaultMessage());
                    return errorMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", "Validation failed");
        body.put("errors", errors);
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        if (log.isDebugEnabled()) {
            log.debug("Constraint violation: {}", ex.getMessage());
        }

        List<Map<String, Object>> errors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    Map<String, Object> errorMap = new HashMap<>();
                    String propertyPath = violation.getPropertyPath().toString();
                    int lastDotIndex = propertyPath.lastIndexOf('.');
                    String fieldName = lastDotIndex >= 0 
                        ? propertyPath.substring(lastDotIndex + 1) 
                        : propertyPath;
                    errorMap.put("field", fieldName);
                    errorMap.put("rejectedValue", violation.getInvalidValue());
                    errorMap.put("message", violation.getMessage());
                    return errorMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", "Validation failed");
        body.put("errors", errors);
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("Request size exceeded limit: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.PAYLOAD_TOO_LARGE.value());
        body.put("error", HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase());
        body.put("message", "Request size exceeds maximum allowed size");
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", "Malformed request body. Please check your JSON format.");
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        if (log.isDebugEnabled()) {
            log.debug("Method not supported: {}", ex.getMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.METHOD_NOT_ALLOWED.value());
        body.put("error", HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase());
        body.put("message", "HTTP method " + ex.getMethod() + " is not supported for this endpoint");
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }
}
