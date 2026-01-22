package com.gettgi.mvp.telemetry.impl;

import com.gettgi.mvp.dto.telemetry.GeoPointDto;
import com.gettgi.mvp.dto.telemetry.RealtimePositionDto;
import com.gettgi.mvp.dto.telemetry.TelemetryIngestDto;
import com.gettgi.mvp.entity.Animal;
import com.gettgi.mvp.entity.Device;
import com.gettgi.mvp.entity.Telemetry;
import com.gettgi.mvp.entity.User;
import com.gettgi.mvp.entity.enums.StatusCollar;
import com.gettgi.mvp.entity.enums.StatutTransmission;
import com.gettgi.mvp.repository.DeviceRepository;
import com.gettgi.mvp.telemetry.RealtimeMessagingService;
import com.gettgi.mvp.telemetry.TelemetryAlertResult;
import com.gettgi.mvp.telemetry.TelemetryAlertService;
import com.gettgi.mvp.telemetry.TelemetryPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelemetryIngestionServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private TelemetryAlertService telemetryAlertService;
    @Mock
    private RealtimeMessagingService realtimeMessagingService;
    @Mock
    private TelemetryPersistenceService telemetryPersistenceService;
    @Mock
    private TelemetryOfflineMonitorService telemetryOfflineMonitorService;

    private GeometryFactory geometryFactory;
    private TelemetryIngestionServiceImpl service;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory();
        service = new TelemetryIngestionServiceImpl(
                deviceRepository,
                geometryFactory,
                telemetryAlertService,
                realtimeMessagingService,
                telemetryPersistenceService,
                telemetryOfflineMonitorService
        );
    }

    @Test
    void ingestShouldPersistTelemetryAndNotifyRealtime() {
        Device device = buildDevice();
        Animal animal = device.getAnimal();
        Instant now = Instant.now();
        TelemetryIngestDto dto = new TelemetryIngestDto(
                device.getImei(),
                now,
                new GeoPointDto(14.5, -17.4),
                12.5,
                0.1,
                0.2,
                0.3,
                1.01,
                80,
                4,
                StatusCollar.VOLE,
                StatutTransmission.ACKNOWLEDGE
        );

        when(deviceRepository.findByImei(device.getImei())).thenReturn(Optional.of(device));
        TelemetryAlertResult alertResult = new TelemetryAlertResult(
                true,
                null,
                null,
                List.of(),
                List.of()
        );
        when(telemetryAlertService.evaluate(any(Device.class), any(Telemetry.class))).thenReturn(alertResult);

        service.ingest(dto);

        ArgumentCaptor<Telemetry> telemetryCaptor = ArgumentCaptor.forClass(Telemetry.class);
        verify(telemetryPersistenceService).buffer(telemetryCaptor.capture());
        Telemetry buffered = telemetryCaptor.getValue();
        assertThat(buffered.getDevice()).isEqualTo(device);
        assertThat(buffered.getSpeed()).isEqualTo(12.5);
        assertThat(buffered.getTransmissionStatus()).isEqualTo(StatutTransmission.ACKNOWLEDGE);
        assertThat(buffered.getStatusCollar()).isEqualTo(StatusCollar.VOLE);
        assertThat(buffered.getTs()).isEqualTo(now);
        Point position = buffered.getPosition();
        assertThat(position.getX()).isEqualTo(-17.4);
        assertThat(position.getY()).isEqualTo(14.5);
        assertThat(position.getSRID()).isEqualTo(4326);

        assertThat(device.getStatusCollar()).isEqualTo(StatusCollar.VOLE);
        assertThat(animal.getLastPosition()).isEqualTo(buffered.getPosition());
        assertThat(animal.getLastPositionTs()).isEqualTo(now);

        ArgumentCaptor<RealtimePositionDto> positionCaptor = ArgumentCaptor.forClass(RealtimePositionDto.class);
        verify(realtimeMessagingService).publishPosition(any(), positionCaptor.capture());
        RealtimePositionDto positionDto = positionCaptor.getValue();
        assertThat(positionDto.animalId()).isEqualTo(animal.getId());
        assertThat(positionDto.deviceId()).isEqualTo(device.getId());
        assertThat(positionDto.collarStatus()).isEqualTo(StatusCollar.VOLE);
        assertThat(positionDto.position().latitude()).isEqualTo(14.5);
        assertThat(positionDto.position().longitude()).isEqualTo(-17.4);

        verify(realtimeMessagingService, never()).publishAlert(any(), any());
    }

    @Test
    void ingestShouldSkipUnknownDevice() {
        TelemetryIngestDto dto = new TelemetryIngestDto(
                "unknown",
                Instant.now(),
                new GeoPointDto(0, 0),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(deviceRepository.findByImei("unknown")).thenReturn(Optional.empty());

        service.ingest(dto);

        verify(telemetryPersistenceService, never()).buffer(any());
        verify(telemetryAlertService, never()).evaluate(any(), any());
        verify(realtimeMessagingService, never()).publishPosition(any(), any());
        verify(realtimeMessagingService, never()).publishAlert(any(), any());
    }

    private Device buildDevice() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setTelephone("221770000001");

        Animal animal = new Animal();
        animal.setId(UUID.randomUUID());
        animal.setNom("Test");
        animal.setUser(user);

        Device device = new Device();
        device.setId(UUID.randomUUID());
        device.setImei("123456789012345");
        device.setStatusCollar(StatusCollar.ACTIF);
        device.setAnimal(animal);
        animal.setDevice(device);
        return device;
    }
}
