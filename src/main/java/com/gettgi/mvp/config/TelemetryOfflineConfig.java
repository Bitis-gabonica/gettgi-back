package com.gettgi.mvp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TelemetryOfflineProperties.class)
public class TelemetryOfflineConfig {
}

