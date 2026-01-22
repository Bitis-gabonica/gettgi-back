package com.gettgi.mvp.dto.telemetry.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for {@link ValidLatitude} annotation.
 */
public class LatitudeValidator implements ConstraintValidator<ValidLatitude, Double> {

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;

    @Override
    public void initialize(ValidLatitude constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Double latitude, ConstraintValidatorContext context) {
        if (latitude == null) {
            return true; // Let @NotNull handle null validation
        }
        return latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE;
    }
}
