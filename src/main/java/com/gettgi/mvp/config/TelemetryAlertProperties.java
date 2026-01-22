package com.gettgi.mvp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.telemetry.alerts")
public class TelemetryAlertProperties {

    /**
     * Pressure threshold below which we consider the collar is being tampered with.
     */
    private double pressureTamperingThreshold = 0.6;

    /**
     * Pressure threshold below which we consider the collar has been cut (theft).
     */
    private double pressureCutThreshold = 0.2;

    /**
     * Speed threshold (in m/s) above which the movement is considered suspicious.
     */
    private double speedSuspectThresholdMps = 18.0; // ~65 km/h

    /**
     * Battery percentage threshold below which we raise a low battery alert.
     */
    private int batteryLowThreshold = 20;

    /**
     * Battery percentage threshold above which we resolve the low battery alert.
     */
    private int batteryRecoveryThreshold = 25;
}

