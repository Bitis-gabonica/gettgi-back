package com.gettgi.mvp.dto.telemetry.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for {@link ValidLongitude} annotation.
 */
public class LongitudeValidator implements ConstraintValidator<ValidLongitude, Double> {

    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    @Override
    public void initialize(ValidLongitude constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Double longitude, ConstraintValidatorContext context) {
        if (longitude == null) {
            return true; // Let @NotNull handle null validation
        }
        return longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE;
    }
}
