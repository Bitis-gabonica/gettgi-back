package com.gettgi.mvp.telemetry;

import com.gettgi.mvp.entity.Telemetry;

public interface TelemetryPersistenceService {

    void buffer(Telemetry telemetry);
}

