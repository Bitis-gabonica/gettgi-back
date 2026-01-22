package com.gettgi.mvp.controller.validation;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class PaginationValidatorTest {

    @Test
    void testValidateAndNormalize_ValidInput() {
        // Given
        int page = 0;
        int size = 10;

        // When
        int[] result = PaginationValidator.validateAndNormalize(page, size);

        // Then
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(0, result[0]);
        assertEquals(10, result[1]);
    }

    @Test
    void testValidateAndNormalize_ValidMaxSize() {
        // Given
        int page = 5;
        int size = 100; // Max allowed

        // When
        int[] result = PaginationValidator.validateAndNormalize(page, size);

        // Then
        assertNotNull(result);
        assertEquals(5, result[0]);
        assertEquals(100, result[1]);
    }

    @Test
    void testValidateAndNormalize_NegativePage() {
        // Given
        int page = -1;
        int size = 10;

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> PaginationValidator.validateAndNormalize(page, size)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Page number must be"));
    }

    @Test
    void testValidateAndNormalize_SizeTooSmall() {
        // Given
        int page = 0;
        int size = 0;

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> PaginationValidator.validateAndNormalize(page, size)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Page size must be"));
    }

    @Test
    void testValidateAndNormalize_SizeTooLarge() {
        // Given
        int page = 0;
        int size = 101; // Exceeds max

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> PaginationValidator.validateAndNormalize(page, size)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Page size must be"));
    }

    @Test
    void testValidate_ValidInput() {
        // Given
        int page = 0;
        int size = 10;

        // When & Then - should not throw
        assertDoesNotThrow(() -> PaginationValidator.validate(page, size));
    }

    @Test
    void testValidate_InvalidInput() {
        // Given
        int page = -1;
        int size = 10;

        // When & Then
        assertThrows(
                ResponseStatusException.class,
                () -> PaginationValidator.validate(page, size)
        );
    }

    @Test
    void testConstants() {
        // Verify constants are accessible
        assertEquals(0, PaginationValidator.MIN_PAGE);
        assertEquals(1, PaginationValidator.MIN_SIZE);
        assertEquals(100, PaginationValidator.MAX_SIZE);
        assertEquals(10, PaginationValidator.DEFAULT_SIZE);
    }
}
