package com.gettgi.mvp.telemetry.impl;

import com.gettgi.mvp.config.TelemetryAlertProperties;
import com.gettgi.mvp.dto.telemetry.AlertNotificationDto;
import com.gettgi.mvp.entity.Alerte;
import com.gettgi.mvp.entity.Animal;
import com.gettgi.mvp.entity.Device;
import com.gettgi.mvp.entity.Geofence;
import com.gettgi.mvp.entity.Telemetry;
import com.gettgi.mvp.entity.User;
import com.gettgi.mvp.entity.enums.StatusCollar;
import com.gettgi.mvp.entity.enums.TypeAlerte;
import com.gettgi.mvp.repository.AlerteRepository;
import com.gettgi.mvp.telemetry.TelemetryAlertResult;
import com.gettgi.mvp.telemetry.TelemetryAlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelemetryAlertServiceImplTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Mock
    private AlerteRepository alerteRepository;

    private TelemetryAlertService service;

    @BeforeEach
    void setUp() {
        service = new TelemetryAlertServiceImpl(alerteRepository, new TelemetryAlertProperties());
    }

    @Test
    void shouldCreateGeofenceExitAlertWhenAnimalLeavesZone() {
        Device device = buildDevice();
        Animal animal = device.getAnimal();
        Geofence geofence = buildGeofence(device.getAnimal().getUser(), "Ferme", 0, 0, 10, 10);
        Telemetry telemetry = buildTelemetry(device, createPoint(20, 20)); // outside zone

        AtomicReference<List<Alerte>> activeAlerts = new AtomicReference<>(List.of());

        lenient().when(alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(animal.getId(), TypeAlerte.GEOFENCE_EXIT))
                .thenReturn(Optional.empty());
        lenient().when(alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(animal.getId(), TypeAlerte.VOL))
                .thenReturn(Optional.empty());
        when(alerteRepository.save(any(Alerte.class))).thenAnswer(invocation -> {
            Alerte alert = invocation.getArgument(0);
            if (alert.getId() == null) {
                alert.setId(UUID.randomUUID());
            }
            activeAlerts.set(alert.isResolved() ? List.of() : List.of(alert));
            return alert;
        });
        when(alerteRepository.findByAnimal_IdAndResolvedFalse(animal.getId()))
                .thenAnswer(invocation -> activeAlerts.get());

        TelemetryAlertResult result = service.evaluate(device, telemetry);

        assertThat(result.insideGeofence()).isFalse();
        assertThat(animal.getLastPositionInsideGeofence()).isFalse();
        assertThat(result.notifications()).hasSize(1);

        AlertNotificationDto notification = result.notifications().get(0);
        assertThat(notification.type()).isEqualTo(TypeAlerte.GEOFENCE_EXIT);
        assertThat(notification.resolved()).isFalse();
        assertThat(notification.message()).contains("Bessie").contains("Ferme").contains("Danger");
        assertThat(result.activeAlerts()).containsExactly(TypeAlerte.GEOFENCE_EXIT);
    }

    @Test
    void shouldResolveGeofenceAlertWhenAnimalReturnsInsideZone() {
        Device device = buildDevice();
        Animal animal = device.getAnimal();
        Geofence geofence = buildGeofence(device.getAnimal().getUser(), "Ferme", 0, 0, 10, 10);
        Telemetry telemetry = buildTelemetry(device, createPoint(5, 5)); // inside zone

        Alerte existingAlert = buildAlert(TypeAlerte.GEOFENCE_EXIT, animal, device, false);
        AtomicReference<List<Alerte>> activeAlerts = new AtomicReference<>(List.of(existingAlert));

        when(alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(animal.getId(), TypeAlerte.GEOFENCE_EXIT))
                .thenReturn(Optional.of(existingAlert));
        lenient().when(alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(animal.getId(), TypeAlerte.VOL))
                .thenReturn(Optional.empty());
        when(alerteRepository.save(existingAlert)).thenAnswer(invocation -> {
            Alerte alert = invocation.getArgument(0);
            activeAlerts.set(alert.isResolved() ? List.of() : List.of(alert));
            return alert;
        });
        when(alerteRepository.findByAnimal_IdAndResolvedFalse(animal.getId()))
                .thenAnswer(invocation -> activeAlerts.get());

        TelemetryAlertResult result = service.evaluate(device, telemetry);

        assertThat(result.insideGeofence()).isTrue();
        assertThat(animal.getLastPositionInsideGeofence()).isTrue();
        assertThat(result.activeAlerts()).isEmpty();
        assertThat(result.notifications()).hasSize(1);

        AlertNotificationDto notification = result.notifications().get(0);
        assertThat(notification.type()).isEqualTo(TypeAlerte.GEOFENCE_EXIT);
        assertThat(notification.resolved()).isTrue();
        assertThat(notification.message()).contains("de retour").contains("Bessie");
    }

    @Test
    void shouldCreateTheftAlertWhenCollarStatusIsStolen() {
        Device device = buildDevice();
        Animal animal = device.getAnimal();
        Telemetry telemetry = buildTelemetry(device, createPoint(5, 5));
        telemetry.setStatusCollar(StatusCollar.VOLE);

        AtomicReference<List<Alerte>> activeAlerts = new AtomicReference<>(List.of());

        lenient().when(alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(animal.getId(), TypeAlerte.GEOFENCE_EXIT))
                .thenReturn(Optional.empty());
        when(alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(animal.getId(), TypeAlerte.COLLAR_CUT))
                .thenReturn(Optional.empty());
        when(alerteRepository.save(any(Alerte.class))).thenAnswer(invocation -> {
            Alerte alert = invocation.getArgument(0);
            if (alert.getId() == null) {
                alert.setId(UUID.randomUUID());
            }
            activeAlerts.set(alert.isResolved() ? List.of() : List.of(alert));
            return alert;
        });
        when(alerteRepository.findByAnimal_IdAndResolvedFalse(animal.getId()))
                .thenAnswer(invocation -> activeAlerts.get());

        TelemetryAlertResult result = service.evaluate(device, telemetry);

        assertThat(result.notifications()).hasSize(1);
        AlertNotificationDto notification = result.notifications().get(0);
        assertThat(notification.type()).isEqualTo(TypeAlerte.COLLAR_CUT);
        assertThat(notification.resolved()).isFalse();
        assertThat(notification.message()).contains("Danger").contains("coupé");
        assertThat(result.activeAlerts()).containsExactly(TypeAlerte.COLLAR_CUT);
    }

    @Test
    void shouldCreateBatteryLowAlertWhenBatteryDropsBelowThreshold() {
        Device device = buildDevice();
        Animal animal = device.getAnimal();
        Telemetry telemetry = buildTelemetry(device, createPoint(5, 5));
        telemetry.setBatteryLevel(10);

        AtomicReference<List<Alerte>> activeAlerts = new AtomicReference<>(List.of());

        lenient().when(alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(animal.getId(), TypeAlerte.GEOFENCE_EXIT))
                .thenReturn(Optional.empty());
        when(alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(animal.getId(), TypeAlerte.BATTERIELOW))
                .thenReturn(Optional.empty());
        when(alerteRepository.findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(animal.getId(), TypeAlerte.COLLAR_CUT))
                .thenReturn(Optional.empty());

        when(alerteRepository.save(any(Alerte.class))).thenAnswer(invocation -> {
            Alerte alert = invocation.getArgument(0);
            if (alert.getId() == null) {
                alert.setId(UUID.randomUUID());
            }
            activeAlerts.set(alert.isResolved() ? List.of() : List.of(alert));
            return alert;
        });
        when(alerteRepository.findByAnimal_IdAndResolvedFalse(animal.getId()))
                .thenAnswer(invocation -> activeAlerts.get());

        TelemetryAlertResult result = service.evaluate(device, telemetry);

        assertThat(result.notifications()).hasSize(1);
        AlertNotificationDto notification = result.notifications().get(0);
        assertThat(notification.type()).isEqualTo(TypeAlerte.BATTERIELOW);
        assertThat(notification.resolved()).isFalse();
        assertThat(notification.message()).contains("Batterie faible");
        assertThat(result.activeAlerts()).containsExactly(TypeAlerte.BATTERIELOW);
    }

    private Device buildDevice() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Animal animal = new Animal();
        animal.setId(UUID.randomUUID());
        animal.setUser(user);
        animal.setNom("Bessie");

        Device device = new Device();
        device.setId(UUID.randomUUID());
        device.setImei("123456789012345");
        device.setStatusCollar(StatusCollar.ACTIF);
        device.setAnimal(animal);
        animal.setDevice(device);
        return device;
    }

    private Telemetry buildTelemetry(Device device, Point position) {
        Telemetry telemetry = new Telemetry();
        telemetry.setId(UUID.randomUUID());
        telemetry.setDevice(device);
        telemetry.setPosition(position);
        telemetry.setTs(Instant.now());
        return telemetry;
    }

    private Geofence buildGeofence(User user, String name, double minLon, double minLat, double maxLon, double maxLat) {
        Geofence geofence = new Geofence();
        geofence.setId(UUID.randomUUID());
        geofence.setNom(name);
        geofence.setZone(createBox(minLon, minLat, maxLon, maxLat));
        geofence.setUser(user);
        user.setGeofence(geofence);
        return geofence;
    }

    private Polygon createBox(double minLon, double minLat, double maxLon, double maxLat) {
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(minLon, minLat),
                new Coordinate(maxLon, minLat),
                new Coordinate(maxLon, maxLat),
                new Coordinate(minLon, maxLat),
                new Coordinate(minLon, minLat)
        };
        Polygon polygon = GEOMETRY_FACTORY.createPolygon(coordinates);
        polygon.setSRID(4326);
        return polygon;
    }

    private Point createPoint(double lon, double lat) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(lon, lat));
        point.setSRID(4326);
        return point;
    }

    private Alerte buildAlert(TypeAlerte type, Animal animal, Device device, boolean resolved) {
        Alerte alert = new Alerte();
        alert.setId(UUID.randomUUID());
        alert.setTypeAlerte(type);
        alert.setAnimal(animal);
        alert.setUser(animal.getUser());
        alert.setDevice(device);
        alert.setTs(Instant.now().minusSeconds(60));
        alert.setResolved(resolved);
        return alert;
    }
}
