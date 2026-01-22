package com.gettgi.mvp.dto.telemetry.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a longitude value is between -180.0 and 180.0 degrees (WGS84).
 */
@Documented
@Constraint(validatedBy = LongitudeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLongitude {
    String message() default "Longitude must be between -180.0 and 180.0 degrees";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
