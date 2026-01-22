package com.gettgi.mvp.config;

/**
 * Constants for validation limits used across the application.
 */
public final class ValidationConstants {

    private ValidationConstants() {
        // Utility class
    }

    // Pagination limits
    public static final int MIN_PAGE = 0;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 10;

    // Field size limits
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_ADDRESS_LENGTH = 200;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 100;
    public static final int MAX_TOKEN_LENGTH = 512;
    public static final int MAX_MESSAGE_LENGTH = 512;
    public static final int MAX_FIRMWARE_VERSION_LENGTH = 32;
    public static final int IMEI_LENGTH = 15;

    // List size limits
    public static final int MAX_VACCINS_PER_ANIMAL = 50;

    // Geographic limits
    public static final double MIN_LATITUDE = -90.0;
    public static final double MAX_LATITUDE = 90.0;
    public static final double MIN_LONGITUDE = -180.0;
    public static final double MAX_LONGITUDE = 180.0;
    public static final double MAX_GEOFENCE_RADIUS_METERS = 100000.0; // ~100km

    // Numeric limits
    public static final int MIN_AGE = 0;
    public static final double MIN_WEIGHT = 0.0;
    public static final double MIN_SIZE = 0.0;
    public static final double MIN_RADIUS_METERS = 10.0;
}
