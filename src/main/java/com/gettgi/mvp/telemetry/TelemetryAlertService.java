package com.gettgi.mvp.telemetry;

import com.gettgi.mvp.entity.Device;
import com.gettgi.mvp.entity.Telemetry;

public interface TelemetryAlertService {

    TelemetryAlertResult evaluate(Device device, Telemetry telemetry);
}

