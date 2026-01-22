package com.gettgi.mvp.telemetry.impl;

import com.gettgi.mvp.dto.telemetry.AlertNotificationDto;
import com.gettgi.mvp.dto.telemetry.GeoPointDto;
import com.gettgi.mvp.dto.telemetry.RealtimePositionDto;
import com.gettgi.mvp.dto.telemetry.TelemetryPointDto;
import com.gettgi.mvp.entity.Alerte;
import com.gettgi.mvp.entity.Animal;
import com.gettgi.mvp.entity.Device;
import com.gettgi.mvp.entity.Geofence;
import com.gettgi.mvp.entity.Telemetry;
import com.gettgi.mvp.repository.AlerteRepository;
import com.gettgi.mvp.repository.AnimalRepository;
import com.gettgi.mvp.repository.TelemetryRepository;
import com.gettgi.mvp.telemetry.TelemetryQueryService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TelemetryQueryServiceImpl implements TelemetryQueryService {

    private final AnimalRepository animalRepository;
    private final TelemetryRepository telemetryRepository;
    private final AlerteRepository alerteRepository;

    @Override
    public RealtimePositionDto getLatestPosition(UUID animalId, String userTelephone) {
        Animal animal = resolveAnimal(animalId, userTelephone);
        Device device = resolveDevice(animal);

        Telemetry telemetry = telemetryRepository.findTopByDevice_IdOrderByTsDesc(device.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No telemetry available for animal"));

        GeoPointDto position = toGeoPoint(telemetry.getPosition());
        boolean insideGeofence = animal.getLastPositionInsideGeofence() == null || animal.getLastPositionInsideGeofence();
        Geofence geofence = animal.getUser() != null ? animal.getUser().getGeofence() : null;

        List<AlertNotificationDto> activeAlerts = mapAlerts(alerteRepository.findByAnimal_IdAndResolvedFalse(animal.getId()));

        return new RealtimePositionDto(
                animal.getId(),
                animalLabel(animal),
                device.getId(),
                device.getImei(),
                position,
                telemetry.getSpeed(),
                telemetry.getBatteryLevel(),
                telemetry.getGsmSignal(),
                telemetry.getStatusCollar() != null ? telemetry.getStatusCollar() : device.getStatusCollar(),
                telemetry.getTs(),
                insideGeofence,
                geofence != null ? geofence.getId() : null,
                geofence != null ? geofence.getNom() : null,
                activeAlerts.stream().map(AlertNotificationDto::type).distinct().toList()
        );
    }

    @Override
    public Page<TelemetryPointDto> getHistory(UUID animalId, String userTelephone, Instant start, Instant end, Pageable pageable) {
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start must be before end");
        }

        Animal animal = resolveAnimal(animalId, userTelephone);
        Device device = resolveDevice(animal);

        return telemetryRepository.findAllByDevice_IdAndTsBetween(device.getId(), start, end, pageable)
                .map(this::toTelemetryPointDto);
    }

    @Override
    public List<AlertNotificationDto> getActiveAlerts(UUID animalId, String userTelephone) {
        Animal animal = resolveAnimal(animalId, userTelephone);
        return mapAlerts(alerteRepository.findByAnimal_IdAndResolvedFalse(animal.getId()));
    }

    @Override
    public Page<AlertNotificationDto> getAlertHistory(UUID animalId, String userTelephone, Instant start, Instant end, Pageable pageable) {
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start must be before end");
        }

        Animal animal = resolveAnimal(animalId, userTelephone);
        return alerteRepository.findAllByAnimal_IdAndTsBetweenOrderByTsDesc(animal.getId(), start, end, pageable)
                .map(this::toAlertDto);
    }

    private Animal resolveAnimal(UUID animalId, String userTelephone) {
        return animalRepository.findByIdAndUserTelephone(animalId, userTelephone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found"));
    }

    private Device resolveDevice(Animal animal) {
        Device device = animal.getDevice();
        if (device == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Animal is not associated with a device");
        }
        return device;
    }

    private TelemetryPointDto toTelemetryPointDto(Telemetry telemetry) {
        return new TelemetryPointDto(
                telemetry.getTs(),
                toGeoPoint(telemetry.getPosition()),
                telemetry.getSpeed(),
                telemetry.getAccelX(),
                telemetry.getAccelY(),
                telemetry.getAccelZ(),
                telemetry.getPressure(),
                telemetry.getBatteryLevel(),
                telemetry.getGsmSignal(),
                telemetry.getStatusCollar(),
                telemetry.getTransmissionStatus()
        );
    }

    private GeoPointDto toGeoPoint(Point point) {
        if (point == null) {
            return null;
        }
        return new GeoPointDto(point.getY(), point.getX());
    }

    private List<AlertNotificationDto> mapAlerts(List<Alerte> alertes) {
        return alertes.stream()
                .map(this::toAlertDto)
                .toList();
    }

    private String animalLabel(Animal animal) {
        return Objects.requireNonNullElse(animal.getNom(), animal.getEspece() != null ? animal.getEspece().name() : "inconnu");
    }

    private AlertNotificationDto toAlertDto(Alerte alert) {
        UUID deviceId = alert.getDevice() != null ? alert.getDevice().getId() : null;
        return new AlertNotificationDto(
                alert.getId(),
                alert.getAnimal().getId(),
                deviceId,
                alert.getTypeAlerte(),
                alert.getMessage(),
                alert.getTs(),
                alert.isResolved(),
                alert.getResolvedAt()
        );
    }
}
