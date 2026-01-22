package com.gettgi.mvp.telemetry.impl;

import com.gettgi.mvp.dto.telemetry.GeoPointDto;
import com.gettgi.mvp.dto.telemetry.RealtimePositionDto;
import com.gettgi.mvp.dto.telemetry.TelemetryIngestDto;
import com.gettgi.mvp.entity.Animal;
import com.gettgi.mvp.entity.Device;
import com.gettgi.mvp.entity.Telemetry;
import com.gettgi.mvp.entity.enums.StatutTransmission;
import com.gettgi.mvp.repository.DeviceRepository;
import com.gettgi.mvp.telemetry.RealtimeMessagingService;
import com.gettgi.mvp.telemetry.TelemetryAlertResult;
import com.gettgi.mvp.telemetry.TelemetryAlertService;
import com.gettgi.mvp.telemetry.TelemetryIngestionService;
import com.gettgi.mvp.telemetry.TelemetryPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryIngestionServiceImpl implements TelemetryIngestionService {

    private final DeviceRepository deviceRepository;
    private final GeometryFactory geometryFactory;
    private final TelemetryAlertService telemetryAlertService;
    private final RealtimeMessagingService realtimeMessagingService;
    private final TelemetryPersistenceService telemetryPersistenceService;
    private final TelemetryOfflineMonitorService telemetryOfflineMonitorService;

    @Override
    @Transactional
    public void ingest(TelemetryIngestDto dto) {
        Optional<Device> maybeDevice = deviceRepository.findByImei(dto.deviceImei());
        if (maybeDevice.isEmpty()) {
            log.warn("Telemetry dropped: unknown device imei={}", dto.deviceImei());
            return;
        }

        Device device = maybeDevice.get();

        Telemetry telemetry = new Telemetry();
        telemetry.setDevice(device);
        telemetry.setPosition(toPoint(dto.position()));
        telemetry.setSpeed(dto.speed());
        telemetry.setAccelX(dto.accelX());
        telemetry.setAccelY(dto.accelY());
        telemetry.setAccelZ(dto.accelZ());
        telemetry.setPressure(dto.pressure());
        telemetry.setBatteryLevel(dto.batteryLevel());
        telemetry.setGsmSignal(dto.gsmSignal());
        telemetry.setTransmissionStatus(dto.transmissionStatusOptional().orElse(StatutTransmission.ENVOYE));
        telemetry.setStatusCollar(dto.statusCollarOptional().orElse(device.getStatusCollar()));
        telemetry.setTs(dto.timestamp());

        dto.statusCollarOptional().ifPresent(device::setStatusCollar);

        Animal linkedAnimal = device.getAnimal();
        if (linkedAnimal != null) {
            linkedAnimal.setLastPosition(telemetry.getPosition());
            linkedAnimal.setLastPositionTs(telemetry.getTs());
        } else {
            log.debug("Device {} has no associated animal; skipping last position update.", device.getImei());
        }

        if (linkedAnimal != null) {
            telemetryOfflineMonitorService.resolveIfTrackerBackOnline(linkedAnimal, device, telemetry.getTs());
        }

        TelemetryAlertResult alertResult = telemetryAlertService.evaluate(device, telemetry);

        String userTelephone = null;
        if (linkedAnimal != null && linkedAnimal.getUser() != null) {
            userTelephone = linkedAnimal.getUser().getTelephone();
        }

        if (linkedAnimal != null) {
            RealtimePositionDto positionDto = new RealtimePositionDto(
                    linkedAnimal.getId(),
                    animalLabel(linkedAnimal),
                    device.getId(),
                    device.getImei(),
                    toGeoPoint(telemetry.getPosition()),
                    telemetry.getSpeed(),
                    telemetry.getBatteryLevel(),
                    telemetry.getGsmSignal(),
                    telemetry.getStatusCollar() != null ? telemetry.getStatusCollar() : device.getStatusCollar(),
                    telemetry.getTs(),
                    alertResult.insideGeofence(),
                    alertResult.geofenceId(),
                    alertResult.geofenceName(),
                    alertResult.activeAlerts()
            );
            if (userTelephone != null && !userTelephone.isBlank()) {
                realtimeMessagingService.publishPosition(userTelephone, positionDto);
            }
        }

        if (userTelephone != null && !userTelephone.isBlank()) {
            String finalUserTelephone = userTelephone;
            alertResult.notifications().forEach(alert -> realtimeMessagingService.publishAlert(finalUserTelephone, alert));
        }

        telemetryPersistenceService.buffer(telemetry);

        if (log.isDebugEnabled()) {
            log.debug("Telemetry buffered for device {} at {}", device.getImei(), telemetry.getTs());
        }
    }

    private Point toPoint(GeoPointDto geoPointDto) {
        Coordinate coordinate = new Coordinate(geoPointDto.longitude(), geoPointDto.latitude());
        Point point = geometryFactory.createPoint(coordinate);
        point.setSRID(4326);
        return point;
    }

    private GeoPointDto toGeoPoint(Point point) {
        return new GeoPointDto(point.getY(), point.getX());
    }

    private String animalLabel(Animal animal) {
        if (animal == null) {
            return "inconnu";
        }
        return animal.getNom() != null ? animal.getNom() : animal.getEspece().name();
    }
}
