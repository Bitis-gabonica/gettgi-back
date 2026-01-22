package com.gettgi.mvp.controller.validation;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Utility class for validating and normalizing pagination parameters.
 */
public final class PaginationValidator {

    public static final int MIN_PAGE = 0;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 100;
    public static final int DEFAULT_SIZE = 10;

    private PaginationValidator() {
        // Utility class
    }

    /**
     * Validates and normalizes pagination parameters.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return an array with [normalizedPage, normalizedSize]
     * @throws ResponseStatusException if validation fails
     */
    public static int[] validateAndNormalize(int page, int size) {
        if (page < MIN_PAGE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number must be >= " + MIN_PAGE
            );
        }

        if (size < MIN_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be >= " + MIN_SIZE
            );
        }

        if (size > MAX_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be <= " + MAX_SIZE
            );
        }

        return new int[]{page, size};
    }

    /**
     * Validates pagination parameters without normalization.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @throws ResponseStatusException if validation fails
     */
    public static void validate(int page, int size) {
        validateAndNormalize(page, size);
    }
}
