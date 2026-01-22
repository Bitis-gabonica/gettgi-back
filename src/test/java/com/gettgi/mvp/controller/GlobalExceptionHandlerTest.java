package com.gettgi.mvp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testHandleMethodArgumentNotValid() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("testObject", "nom", "", false, null, null, "must not be blank");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.Collections.singletonList(fieldError));

        // When
        var response = handler.handleMethodArgumentNotValid(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Validation failed", body.get("message"));
        assertTrue(body.containsKey("errors"));
        assertTrue(body.containsKey("timestamp"));
    }

    @Test
    void testHandleConstraintViolation() {
        // Given
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("test.field");
        when(violation.getInvalidValue()).thenReturn("invalid");
        when(violation.getMessage()).thenReturn("must not be null");
        violations.add(violation);

        when(ex.getConstraintViolations()).thenReturn(violations);

        // When
        var response = handler.handleConstraintViolation(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Validation failed", body.get("message"));
        assertTrue(body.containsKey("errors"));
        assertTrue(body.containsKey("timestamp"));
    }

    @Test
    void testHandleMaxUploadSizeExceeded() {
        // Given
        MaxUploadSizeExceededException ex = mock(MaxUploadSizeExceededException.class);

        // When
        var response = handler.handleMaxUploadSizeExceeded(ex);

        // Then
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(413, body.get("status"));
        assertEquals("Payload Too Large", body.get("error"));
        assertEquals("Request size exceeds maximum allowed size", body.get("message"));
        assertTrue(body.containsKey("timestamp"));
    }

    @Test
    void testHandleHttpMessageNotReadable() {
        // Given
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        // When
        var response = handler.handleHttpMessageNotReadable(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertTrue(body.get("message").toString().contains("Malformed request body"));
        assertTrue(body.containsKey("timestamp"));
    }

    @Test
    void testHandleResponseStatus() {
        // Given
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");

        // When
        var response = handler.handleResponseStatus(ex);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(404, body.get("status"));
        assertEquals("Resource not found", body.get("error"));
        assertEquals("Resource not found", body.get("message"));
    }

    @Test
    void testHandleIllegalState() {
        // Given
        IllegalStateException ex = new IllegalStateException("Resource introuvable");

        // When
        var response = handler.handleIllegalState(ex);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(404, body.get("status"));
    }
}
