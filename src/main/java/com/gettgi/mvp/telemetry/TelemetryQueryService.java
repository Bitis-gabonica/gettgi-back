package com.gettgi.mvp.telemetry;

import com.gettgi.mvp.dto.telemetry.AlertNotificationDto;
import com.gettgi.mvp.dto.telemetry.RealtimePositionDto;
import com.gettgi.mvp.dto.telemetry.TelemetryPointDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TelemetryQueryService {

    RealtimePositionDto getLatestPosition(UUID animalId, String userTelephone);

    Page<TelemetryPointDto> getHistory(UUID animalId, String userTelephone, Instant start, Instant end, Pageable pageable);

    List<AlertNotificationDto> getActiveAlerts(UUID animalId, String userTelephone);

    Page<AlertNotificationDto> getAlertHistory(UUID animalId, String userTelephone, Instant start, Instant end, Pageable pageable);
}
