package com.gettgi.mvp.telemetry.impl;

import com.gettgi.mvp.config.TelemetryAlertProperties;
import com.gettgi.mvp.dto.telemetry.AlertNotificationDto;
import com.gettgi.mvp.entity.Alerte;
import com.gettgi.mvp.entity.Animal;
import com.gettgi.mvp.entity.Device;
import com.gettgi.mvp.entity.Geofence;
import com.gettgi.mvp.entity.Telemetry;
import com.gettgi.mvp.entity.enums.StatusCollar;
import com.gettgi.mvp.entity.enums.TypeAlerte;
import com.gettgi.mvp.repository.AlerteRepository;
import com.gettgi.mvp.telemetry.TelemetryAlertResult;
import com.gettgi.mvp.telemetry.TelemetryAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryAlertServiceImpl implements TelemetryAlertService {

    private final AlerteRepository alerteRepository;
    private final TelemetryAlertProperties alertProperties;

    @Override
    @Transactional
    public TelemetryAlertResult evaluate(Device device, Telemetry telemetry) {
        Animal animal = device.getAnimal();
        if (animal == null) {
            return new TelemetryAlertResult(
                    true,
                    null,
                    null,
                    List.of(),
                    List.of()
            );
        }

        List<AlertNotificationDto> notifications = new ArrayList<>();

        var user = animal.getUser();
        Geofence geofence = user != null ? user.getGeofence() : null;
        boolean insideGeofence = computeInsideGeofence(telemetry, geofence);
        UUID geofenceId = geofence != null ? geofence.getId() : null;
        String geofenceName = geofence != null ? geofence.getNom() : null;
        animal.setLastPositionInsideGeofence(insideGeofence);

        Double distanceMeters = computeDistanceMeters(
                user != null ? user.getPosition() : null,
                telemetry.getPosition()
        );

        notifications.addAll(handleGeofenceAlert(device, telemetry, animal, geofenceName, insideGeofence, distanceMeters));
        notifications.addAll(handleBatteryLowAlert(device, telemetry, animal, distanceMeters));
        notifications.addAll(handlePressureAlert(device, telemetry, animal, distanceMeters));
        notifications.addAll(handleCollarStatusAlert(device, telemetry, animal, distanceMeters));
        notifications.addAll(handleSpeedAlert(device, telemetry, animal, distanceMeters));

        List<TypeAlerte> activeAlerts = alerteRepository.findByAnimal_IdAndResolvedFalse(animal.getId()).stream()
                .map(Alerte::getTypeAlerte)
                .distinct()
                .toList();

        return new TelemetryAlertResult(
                insideGeofence,
                geofenceId,
                geofenceName,
                activeAlerts,
                notifications
        );
    }

    private boolean computeInsideGeofence(Telemetry telemetry, Geofence geofence) {
        if (geofence == null || geofence.getZone() == null) {
            return true;
        }
        try {
            return geofence.getZone().covers(telemetry.getPosition());
        } catch (Exception ex) {
            log.warn("Unable to evaluate geofence containment for telemetry {} and geofence {}",
                    telemetry.getId(), geofence.getId(), ex);
            return true;
        }
    }

    private List<AlertNotificationDto> handleGeofenceAlert(Device device,
                                                           Telemetry telemetry,
                                                           Animal animal,
                                                           String geofenceName,
                                                           boolean insideGeofence,
                                                           Double distanceMeters) {
        List<AlertNotificationDto> notifications = new ArrayList<>();
        var existingAlertOptional = alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(
                animal.getId(),
                TypeAlerte.GEOFENCE_EXIT
        );

        if (!insideGeofence) {
            if (existingAlertOptional.isEmpty()) {
                Alerte alert = buildAlert(TypeAlerte.GEOFENCE_EXIT, telemetry.getTs(), animal, device);
                alert.setMessage(buildGeofenceExitMessage(animal, geofenceName, telemetry, distanceMeters));
                Alerte saved = alerteRepository.save(alert);
                notifications.add(toDto(saved));
            }
        } else {
            existingAlertOptional.ifPresent(alert -> {
                resolveAlert(alert, telemetry.getTs(), buildGeofenceReturnMessage(animal, geofenceName, telemetry, distanceMeters));
                Alerte saved = alerteRepository.save(alert);
                notifications.add(toDto(saved));
            });
        }
        return notifications;
    }

    private List<AlertNotificationDto> handleBatteryLowAlert(Device device,
                                                             Telemetry telemetry,
                                                             Animal animal,
                                                             Double distanceMeters) {
        List<AlertNotificationDto> notifications = new ArrayList<>();
        Integer batteryLevel = telemetry.getBatteryLevel();
        if (batteryLevel == null) {
            return notifications;
        }

        boolean lowBattery = batteryLevel <= alertProperties.getBatteryLowThreshold();
        var existing = alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(
                animal.getId(),
                TypeAlerte.BATTERIELOW
        );

        if (lowBattery) {
            if (existing.isEmpty()) {
                Alerte alert = buildAlert(TypeAlerte.BATTERIELOW, telemetry.getTs(), animal, device);
                alert.setMessage(buildBatteryLowMessage(animal, batteryLevel, telemetry, distanceMeters));
                Alerte saved = alerteRepository.save(alert);
                notifications.add(toDto(saved));
            }
        } else if (batteryLevel >= alertProperties.getBatteryRecoveryThreshold()) {
            existing.ifPresent(alert -> {
                resolveAlert(alert, telemetry.getTs(), buildBatteryLowResolvedMessage(animal, batteryLevel));
                Alerte saved = alerteRepository.save(alert);
                notifications.add(toDto(saved));
            });
        }

        return notifications;
    }

    private List<AlertNotificationDto> handleCollarStatusAlert(Device device,
                                                               Telemetry telemetry,
                                                               Animal animal,
                                                               Double distanceMeters) {
        List<AlertNotificationDto> notifications = new ArrayList<>();
        StatusCollar effectiveStatus = telemetry.getStatusCollar() != null
                ? telemetry.getStatusCollar()
                : device.getStatusCollar();

        boolean cutDetected = effectiveStatus == StatusCollar.VOLE
                || effectiveStatus == StatusCollar.DETRUIT
                || telemetry.getPressure() != null && telemetry.getPressure() <= alertProperties.getPressureCutThreshold();

        var existingCut = alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(
                animal.getId(),
                TypeAlerte.COLLAR_CUT
        );

        if (cutDetected) {
            if (existingCut.isEmpty()) {
                Alerte alert = buildAlert(TypeAlerte.COLLAR_CUT, telemetry.getTs(), animal, device);
                alert.setMessage(buildCollarCutMessage(animal, telemetry, distanceMeters));
                Alerte saved = alerteRepository.save(alert);
                notifications.add(toDto(saved));
            }
        } else {
            existingCut.ifPresent(alert -> {
                resolveAlert(alert, telemetry.getTs(), buildCollarCutResolvedMessage(animal));
                Alerte saved = alerteRepository.save(alert);
                notifications.add(toDto(saved));
            });
        }

        return notifications;
    }

    private List<AlertNotificationDto> handlePressureAlert(Device device,
                                                           Telemetry telemetry,
                                                           Animal animal,
                                                           Double distanceMeters) {
        List<AlertNotificationDto> notifications = new ArrayList<>();
        Double pressure = telemetry.getPressure();
        if (pressure == null) {
            return notifications;
        }

        if (pressure <= alertProperties.getPressureCutThreshold()) {
            // handled by collar cut logic
            return notifications;
        }

        boolean tamperingDetected = pressure <= alertProperties.getPressureTamperingThreshold();
        var existing = alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(
                animal.getId(),
                TypeAlerte.COLLAR_TAMPERING
        );

        if (tamperingDetected) {
            if (existing.isEmpty()) {
                Alerte alert = buildAlert(TypeAlerte.COLLAR_TAMPERING, telemetry.getTs(), animal, device);
                alert.setMessage(buildCollarTamperingMessage(animal, telemetry, distanceMeters));
                Alerte saved = alerteRepository.save(alert);
                notifications.add(toDto(saved));
            }
        } else {
            existing.ifPresent(alert -> {
                resolveAlert(alert, telemetry.getTs(), buildCollarTamperingResolvedMessage(animal));
                Alerte saved = alerteRepository.save(alert);
                notifications.add(toDto(saved));
            });
        }

        return notifications;
    }

    private List<AlertNotificationDto> handleSpeedAlert(Device device,
                                                        Telemetry telemetry,
                                                        Animal animal,
                                                        Double distanceMeters) {
        List<AlertNotificationDto> notifications = new ArrayList<>();
        Double speed = telemetry.getSpeed();
        if (speed == null) {
            return notifications;
        }

        boolean suspiciousSpeed = speed >= alertProperties.getSpeedSuspectThresholdMps();
        var existing = alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(
                animal.getId(),
                TypeAlerte.SPEED_SUSPECT
        );

        if (suspiciousSpeed) {
            if (existing.isEmpty()) {
                Alerte alert = buildAlert(TypeAlerte.SPEED_SUSPECT, telemetry.getTs(), animal, device);
                alert.setMessage(buildSpeedAlertMessage(animal, telemetry, distanceMeters));
                Alerte saved = alerteRepository.save(alert);
                notifications.add(toDto(saved));
            }
        } else {
            existing.ifPresent(alert -> {
                resolveAlert(alert, telemetry.getTs(), buildSpeedResolvedMessage(animal));
                Alerte saved = alerteRepository.save(alert);
                notifications.add(toDto(saved));
            });
        }

        return notifications;
    }

    private Alerte buildAlert(TypeAlerte type, Instant timestamp, Animal animal, Device device) {
        Alerte alert = new Alerte();
        alert.setTypeAlerte(type);
        alert.setTs(timestamp);
        alert.setResolved(false);
        alert.setUser(animal.getUser());
        alert.setAnimal(animal);
        alert.setDevice(device);
        return alert;
    }

    private void resolveAlert(Alerte alert, Instant resolvedAt, String message) {
        alert.setResolved(true);
        alert.setResolvedAt(resolvedAt);
        alert.setMessage(message);
    }

    private AlertNotificationDto toDto(Alerte alert) {
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

    private String buildGeofenceExitMessage(Animal animal, String geofenceName, Telemetry telemetry, Double distanceMeters) {
        return "Danger Animal %s hors de la zone %s. Position=%s, Distance=%s".formatted(
                animalLabel(animal),
                geofenceName != null ? geofenceName : "autorisée",
                formatPosition(telemetry),
                formatDistance(distanceMeters)
        );
    }

    private String buildGeofenceReturnMessage(Animal animal, String geofenceName, Telemetry telemetry, Double distanceMeters) {
        return "Animal %s de retour dans la zone %s. Position=%s, Distance=%s".formatted(
                animalLabel(animal),
                geofenceName != null ? geofenceName : "autorisée",
                formatPosition(telemetry),
                formatDistance(distanceMeters)
        );
    }

    private String buildCollarTamperingMessage(Animal animal, Telemetry telemetry, Double distanceMeters) {
        return "Danger Le collier de l'animal %s est en train de se faire couper. Position=%s, Distance=%s".formatted(
                animalLabel(animal),
                formatPosition(telemetry),
                formatDistance(distanceMeters)
        );
    }

    private String buildCollarTamperingResolvedMessage(Animal animal) {
        return "Le collier de l'animal %s ne semble plus être coupé.".formatted(animalLabel(animal));
    }

    private String buildCollarCutMessage(Animal animal, Telemetry telemetry, Double distanceMeters) {
        return "Danger On a coupé le collier de l'animal %s, il se fait voler. Position=%s, Distance=%s".formatted(
                animalLabel(animal),
                formatPosition(telemetry),
                formatDistance(distanceMeters)
        );
    }

    private String buildCollarCutResolvedMessage(Animal animal) {
        return "Le collier de l'animal %s n'est plus signalé comme coupé.".formatted(animalLabel(animal));
    }

    private String buildSpeedAlertMessage(Animal animal, Telemetry telemetry, Double distanceMeters) {
        return "Danger L'animal %s se déplace à une vitesse anormale (%.2f m/s). Position=%s, Distance=%s".formatted(
                animalLabel(animal),
                telemetry.getSpeed(),
                formatPosition(telemetry),
                formatDistance(distanceMeters)
        );
    }

    private String buildSpeedResolvedMessage(Animal animal) {
        return "La vitesse de l'animal %s est redevenue normale.".formatted(animalLabel(animal));
    }

    private String buildBatteryLowMessage(Animal animal, Integer batteryLevel, Telemetry telemetry, Double distanceMeters) {
        return "Batterie faible (%d%%) pour %s. Position=%s, Distance=%s".formatted(
                batteryLevel,
                animalLabel(animal),
                formatPosition(telemetry),
                formatDistance(distanceMeters)
        );
    }

    private String buildBatteryLowResolvedMessage(Animal animal, Integer batteryLevel) {
        return "Batterie OK (%d%%) pour %s.".formatted(batteryLevel, animalLabel(animal));
    }

    private String animalLabel(Animal animal) {
        if (animal == null) {
            return "inconnu";
        }
        return Objects.requireNonNullElse(animal.getNom(), "inconnu");
    }

    private String formatPosition(Telemetry telemetry) {
        if (telemetry.getPosition() == null) {
            return "inconnue";
        }
        return "%.5f, %.5f".formatted(
                telemetry.getPosition().getY(),
                telemetry.getPosition().getX()
        );
    }

    private String formatDistance(Double distanceMeters) {
        if (distanceMeters == null) {
            return "inconnue";
        }
        if (distanceMeters >= 1000) {
            return "%.2f km".formatted(distanceMeters / 1000d);
        }
        return "%.0f m".formatted(distanceMeters);
    }

    private Double computeDistanceMeters(Point userPosition, Point telemetryPosition) {
        if (userPosition == null || telemetryPosition == null) {
            return null;
        }
        double lat1 = Math.toRadians(userPosition.getY());
        double lon1 = Math.toRadians(userPosition.getX());
        double lat2 = Math.toRadians(telemetryPosition.getY());
        double lon2 = Math.toRadians(telemetryPosition.getX());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371000 * c; // Earth radius in meters
    }
}
