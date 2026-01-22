package com.gettgi.mvp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

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
}
