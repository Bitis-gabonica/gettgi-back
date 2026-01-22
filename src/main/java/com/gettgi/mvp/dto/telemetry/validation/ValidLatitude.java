package com.gettgi.mvp.dto.telemetry.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a latitude value is between -90.0 and 90.0 degrees (WGS84).
 */
@Documented
@Constraint(validatedBy = LatitudeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLatitude {
    String message() default "Latitude must be between -90.0 and 90.0 degrees";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
