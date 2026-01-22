package com.gettgi.mvp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configuration for validation.
 * Spring Boot automatically configures Jakarta Bean Validation,
 * but this class can be used for custom validation configuration if needed.
 */
@Configuration
public class ValidationConfig {

    /**
     * Custom validator factory bean can be configured here if needed.
     * For now, Spring Boot's auto-configuration is sufficient.
     */
    // Example: If you need custom message interpolation or other features
    // @Bean
    // public LocalValidatorFactoryBean validator() {
    //     LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
    //     // Custom configuration
    //     return factory;
    // }
}
