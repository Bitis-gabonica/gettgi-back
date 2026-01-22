package com.gettgi.mvp.telemetry.impl;

import com.gettgi.mvp.config.TelemetryOfflineProperties;
import com.gettgi.mvp.dto.telemetry.AlertNotificationDto;
import com.gettgi.mvp.entity.Alerte;
import com.gettgi.mvp.entity.Animal;
import com.gettgi.mvp.entity.Device;
import com.gettgi.mvp.entity.User;
import com.gettgi.mvp.entity.enums.TypeAlerte;
import com.gettgi.mvp.repository.AlerteRepository;
import com.gettgi.mvp.repository.AnimalRepository;
import com.gettgi.mvp.telemetry.RealtimeMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryOfflineMonitorService {

    private final TelemetryOfflineProperties properties;
    private final AnimalRepository animalRepository;
    private final AlerteRepository alerteRepository;
    private final RealtimeMessagingService realtimeMessagingService;

    @Scheduled(fixedDelayString = "${app.telemetry.offline.check-interval:PT1M}", initialDelay = 30_000)
    @Transactional
    public void checkOfflineTrackers() {
        Duration threshold = properties.getThreshold() != null ? properties.getThreshold() : Duration.ofMinutes(5);
        if (threshold.isZero() || threshold.isNegative()) {
            return;
        }

        Instant now = Instant.now();
        Instant cutoff = now.minus(threshold);

        List<Animal> staleAnimals = animalRepository.findAnimalsWithLastTelemetryBefore(cutoff);
        for (Animal animal : staleAnimals) {
            Device device = animal.getDevice();
            User user = animal.getUser();
            if (device == null || user == null) {
                continue;
            }
            String telephone = user.getTelephone();
            if (telephone == null || telephone.isBlank()) {
                continue;
            }

            boolean alreadyActive = alerteRepository
                    .findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(animal.getId(), TypeAlerte.DEVICE_OFFLINE)
                    .isPresent();
            if (alreadyActive) {
                continue;
            }

            Alerte alert = new Alerte();
            alert.setTypeAlerte(TypeAlerte.DEVICE_OFFLINE);
            alert.setTs(now);
            alert.setResolved(false);
            alert.setResolvedAt(null);
            alert.setUser(user);
            alert.setAnimal(animal);
            alert.setDevice(device);
            alert.setMessage(buildOfflineMessage(animal, device, animal.getLastPositionTs(), now));

            Alerte saved = alerteRepository.save(alert);
            realtimeMessagingService.publishAlert(telephone, toDto(saved));
        }
    }

    @Transactional
    public void resolveIfTrackerBackOnline(Animal animal, Device device, Instant telemetryTs) {
        if (animal == null || device == null) {
            return;
        }

        var existing = alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(
                animal.getId(),
                TypeAlerte.DEVICE_OFFLINE
        );

        existing.ifPresent(alert -> {
            User user = alert.getUser();
            String telephone = user != null ? user.getTelephone() : null;
            if (telephone == null || telephone.isBlank()) {
                return;
            }

            alert.setResolved(true);
            alert.setResolvedAt(telemetryTs != null ? telemetryTs : Instant.now());
            alert.setMessage(buildOnlineMessage(animal, device));

            Alerte saved = alerteRepository.save(alert);
            realtimeMessagingService.publishAlert(telephone, toDto(saved));
        });
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

    private String buildOfflineMessage(Animal animal, Device device, Instant lastSeen, Instant now) {
        String animalLabel = animalLabel(animal);
        String imei = device != null ? Objects.requireNonNullElse(device.getImei(), "inconnu") : "inconnu";
        if (lastSeen == null) {
            return "Tracker hors ligne pour %s (IMEI=%s).".formatted(animalLabel, imei);
        }

        long minutes = 0;
        if (now != null) {
            minutes = Math.max(0, Duration.between(lastSeen, now).toMinutes());
        }

        return "Tracker hors ligne pour %s (IMEI=%s). Derni\u00E8re t\u00E9l\u00E9m\u00E9trie il y a %d min (%s)."
                .formatted(animalLabel, imei, minutes, lastSeen);
    }

    private String buildOnlineMessage(Animal animal, Device device) {
        String animalLabel = animalLabel(animal);
        String imei = device != null ? Objects.requireNonNullElse(device.getImei(), "inconnu") : "inconnu";
        return "Tracker de nouveau en ligne pour %s (IMEI=%s).".formatted(animalLabel, imei);
    }

    private String animalLabel(Animal animal) {
        if (animal == null) {
            return "animal";
        }
        return Objects.requireNonNullElse(animal.getNom(), "animal");
    }
}
