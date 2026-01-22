package com.gettgi.mvp.telemetry.impl;

import com.gettgi.mvp.config.TelemetryPersistenceProperties;
import com.gettgi.mvp.entity.Telemetry;
import com.gettgi.mvp.repository.TelemetryRepository;
import com.gettgi.mvp.telemetry.TelemetryPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryPersistenceServiceImpl implements TelemetryPersistenceService {

    private final TelemetryRepository telemetryRepository;
    private final TelemetryPersistenceProperties properties;

    private final Map<UUID, Telemetry> latestByDevice = new ConcurrentHashMap<>();

    @Override
    public void buffer(Telemetry telemetry) {
        if (telemetry.getDevice() == null || telemetry.getDevice().getId() == null) {
            log.debug("Telemetry ignored for buffering due to missing device identifier.");
            return;
        }
        UUID deviceId = telemetry.getDevice().getId();
        Telemetry copy = cloneTelemetry(telemetry);
        latestByDevice.put(deviceId, copy);

        if (latestByDevice.size() > properties.getMaxEntries()) {
            log.warn("Telemetry buffer reached max capacity ({}). Older entries may be overwritten.", properties.getMaxEntries());
        }
    }

    @Scheduled(fixedDelayString = "#{T(java.time.Duration).parse('${app.telemetry.persistence.flush-interval:PT30S}').toMillis()}")
    @Transactional
    public void flush() {
        if (latestByDevice.isEmpty()) {
            return;
        }
        List<Telemetry> batch = drain();
        if (batch.isEmpty()) {
            return;
        }
        telemetryRepository.saveAll(batch);
        if (log.isDebugEnabled()) {
            log.debug("Persisted {} telemetry points ({} devices).", batch.size(), batch.stream().map(t -> t.getDevice().getId()).distinct().count());
        }
    }

    private List<Telemetry> drain() {
        List<Telemetry> drained = new ArrayList<>(latestByDevice.values());
        latestByDevice.clear();
        return drained;
    }

    private Telemetry cloneTelemetry(Telemetry source) {
        Telemetry copy = new Telemetry();
        copy.setDevice(source.getDevice());
        copy.setPosition(copyPoint(source.getPosition()));
        copy.setSpeed(source.getSpeed());
        copy.setAccelX(source.getAccelX());
        copy.setAccelY(source.getAccelY());
        copy.setAccelZ(source.getAccelZ());
        copy.setPressure(source.getPressure());
        copy.setBatteryLevel(source.getBatteryLevel());
        copy.setGsmSignal(source.getGsmSignal());
        copy.setTransmissionStatus(source.getTransmissionStatus());
        copy.setStatusCollar(source.getStatusCollar());
        copy.setTs(source.getTs());
        return copy;
    }

    private Point copyPoint(Point point) {
        if (point == null) {
            return null;
        }
        Point clone = (Point) point.copy();
        clone.setSRID(point.getSRID());
        return clone;
    }
}
