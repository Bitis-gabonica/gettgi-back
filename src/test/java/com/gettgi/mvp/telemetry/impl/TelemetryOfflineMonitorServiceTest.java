package com.gettgi.mvp.telemetry.impl;

import com.gettgi.mvp.config.TelemetryOfflineProperties;
import com.gettgi.mvp.entity.Alerte;
import com.gettgi.mvp.entity.Animal;
import com.gettgi.mvp.entity.Device;
import com.gettgi.mvp.entity.User;
import com.gettgi.mvp.entity.enums.TypeAlerte;
import com.gettgi.mvp.repository.AlerteRepository;
import com.gettgi.mvp.repository.AnimalRepository;
import com.gettgi.mvp.telemetry.RealtimeMessagingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelemetryOfflineMonitorServiceTest {

    @Mock
    private AnimalRepository animalRepository;
    @Mock
    private AlerteRepository alerteRepository;
    @Mock
    private RealtimeMessagingService realtimeMessagingService;

    private TelemetryOfflineMonitorService service;

    @BeforeEach
    void setUp() {
        TelemetryOfflineProperties properties = new TelemetryOfflineProperties();
        properties.setThreshold(Duration.ofMinutes(5));
        properties.setCheckInterval(Duration.ofMinutes(1));
        service = new TelemetryOfflineMonitorService(properties, animalRepository, alerteRepository, realtimeMessagingService);
    }

    @Test
    void shouldCreateOfflineAlertWhenLastTelemetryIsTooOld() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setTelephone("221770000001");

        Device device = new Device();
        device.setId(UUID.randomUUID());
        device.setImei("123456789012345");

        Animal animal = new Animal();
        animal.setId(UUID.randomUUID());
        animal.setNom("Bessie");
        animal.setUser(user);
        animal.setDevice(device);
        animal.setLastPositionTs(Instant.now().minus(Duration.ofMinutes(10)));

        when(animalRepository.findAnimalsWithLastTelemetryBefore(any(Instant.class))).thenReturn(List.of(animal));
        when(alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(animal.getId(), TypeAlerte.DEVICE_OFFLINE))
                .thenReturn(Optional.empty());
        when(alerteRepository.save(any(Alerte.class))).thenAnswer(invocation -> {
            Alerte alert = invocation.getArgument(0);
            if (alert.getId() == null) {
                alert.setId(UUID.randomUUID());
            }
            return alert;
        });

        service.checkOfflineTrackers();

        verify(realtimeMessagingService).publishAlert(anyString(), any());
    }

    @Test
    void shouldResolveOfflineAlertWhenTelemetryArrives() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setTelephone("221770000001");

        Device device = new Device();
        device.setId(UUID.randomUUID());
        device.setImei("123456789012345");

        Animal animal = new Animal();
        animal.setId(UUID.randomUUID());
        animal.setNom("Bessie");
        animal.setUser(user);
        animal.setDevice(device);

        Alerte existing = new Alerte();
        existing.setId(UUID.randomUUID());
        existing.setTypeAlerte(TypeAlerte.DEVICE_OFFLINE);
        existing.setTs(Instant.now().minus(Duration.ofMinutes(7)));
        existing.setResolved(false);
        existing.setUser(user);
        existing.setAnimal(animal);
        existing.setDevice(device);

        when(alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(animal.getId(), TypeAlerte.DEVICE_OFFLINE))
                .thenReturn(Optional.of(existing));
        when(alerteRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        Instant telemetryTs = Instant.now();
        service.resolveIfTrackerBackOnline(animal, device, telemetryTs);

        assertThat(existing.isResolved()).isTrue();
        assertThat(existing.getResolvedAt()).isEqualTo(telemetryTs);
        verify(realtimeMessagingService).publishAlert(anyString(), any());
    }

    @Test
    void shouldNotCreateOfflineAlertWhenAlreadyActive() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setTelephone("221770000001");

        Device device = new Device();
        device.setId(UUID.randomUUID());
        device.setImei("123456789012345");

        Animal animal = new Animal();
        animal.setId(UUID.randomUUID());
        animal.setUser(user);
        animal.setDevice(device);
        animal.setLastPositionTs(Instant.now().minus(Duration.ofMinutes(10)));

        when(animalRepository.findAnimalsWithLastTelemetryBefore(any(Instant.class))).thenReturn(List.of(animal));
        when(alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(animal.getId(), TypeAlerte.DEVICE_OFFLINE))
                .thenReturn(Optional.of(new Alerte()));

        service.checkOfflineTrackers();

        verify(realtimeMessagingService, never()).publishAlert(anyString(), any());
        verify(alerteRepository, never()).save(any());
    }
}

