package com.gettgi.mvp.telemetry;

import com.gettgi.mvp.dto.telemetry.TelemetryIngestDto;

public interface TelemetryIngestionService {

    void ingest(TelemetryIngestDto dto);
}

