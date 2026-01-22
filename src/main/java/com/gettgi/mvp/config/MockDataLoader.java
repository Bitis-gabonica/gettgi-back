package com.gettgi.mvp.config;

import com.gettgi.mvp.entity.*;
import com.gettgi.mvp.entity.enums.Espece;
import com.gettgi.mvp.entity.enums.Role;
import com.gettgi.mvp.entity.enums.Sexe;
import com.gettgi.mvp.entity.enums.Statut;
import com.gettgi.mvp.entity.enums.StatusCollar;
import com.gettgi.mvp.entity.enums.TypeAlerte;
import com.gettgi.mvp.entity.enums.UserRole;
import com.gettgi.mvp.repository.AlerteRepository;
import com.gettgi.mvp.repository.AnimalRepository;
import com.gettgi.mvp.repository.DeviceRepository;
import com.gettgi.mvp.repository.GeofenceRepository;
import com.gettgi.mvp.repository.TelemetryRepository;
import com.gettgi.mvp.repository.TroupeauRepository;
import com.gettgi.mvp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class MockDataLoader {

    private final UserRepository userRepository;
    private final AlerteRepository alerteRepository;
    private final AnimalRepository animalRepository;
    private final DeviceRepository deviceRepository;
    private final GeofenceRepository geofenceRepository;
    private final TroupeauRepository troupeauRepository;
    private final TelemetryRepository telemetryRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedMockData() {
        return args -> {
            if (userRepository.count() > 0) return;

            GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);

            // Users in Senegal
            User u1 = new User();
            u1.setNom("Diop");
            u1.setPrenom("Mamadou");
            u1.setAdresse("Dakar, Sénégal");
            u1.setTelephone("221770000001");
            u1.setEmail("mamadou.diop@example.com");
            u1.setRole(UserRole.ROLE_USER);
            u1.setPasswordHash(passwordEncoder.encode("password"));
            Point p1 = gf.createPoint(new Coordinate(-17.4677, 14.7167)); // Dakar lon,lat
            p1.setSRID(4326);
            u1.setPosition(p1);

            User u2 = new User();
            u2.setNom("Ndiaye");
            u2.setPrenom("Awa");
            u2.setAdresse("Thiès, Sénégal");
            u2.setTelephone("221770000002");
            u2.setEmail("awa.ndiaye@example.com");
            u2.setRole(UserRole.ROLE_USER);
            u2.setPasswordHash(passwordEncoder.encode("password"));
            Point p2 = gf.createPoint(new Coordinate(-16.9260, 14.7908)); // Thiès lon,lat
            p2.setSRID(4326);
            u2.setPosition(p2);

            userRepository.saveAll(List.of(u1, u2));

            User admin = new User();
            admin.setNom("Admin");
            admin.setPrenom("GettGi");
            admin.setAdresse("Dakar, SAcnAcgal");
            admin.setTelephone("221770000003");
            admin.setEmail("admin.gettgi@example.com");
            admin.setRole(UserRole.ROLE_ADMIN);
            admin.setPasswordHash(passwordEncoder.encode("password"));
            Point p3 = gf.createPoint(new Coordinate(-17.4677, 14.7167)); // Dakar lon,lat
            p3.setSRID(4326);
            admin.setPosition(p3);
            userRepository.save(admin);

            // Troupeaux (one per user)
            Troupeau t1 = new Troupeau();
            t1.setNom("Troupeau Dakar A");
            t1.setUser(u1);
            troupeauRepository.save(t1);

            Troupeau t2 = new Troupeau();
            t2.setNom("Troupeau Thiès A");
            t2.setUser(u2);
            troupeauRepository.save(t2);

            Geofence geoU1 = createGeofence(gf, u1, "Parcelle Dakar",
                    new Coordinate(-17.4705, 14.7145),
                    new Coordinate(-17.4620, 14.7145),
                    new Coordinate(-17.4620, 14.7205),
                    new Coordinate(-17.4705, 14.7205)
            );
            Geofence geoU2 = createGeofence(gf, u2, "Parcelle Thiès",
                    new Coordinate(-16.9285, 14.7885),
                    new Coordinate(-16.9200, 14.7885),
                    new Coordinate(-16.9200, 14.7960),
                    new Coordinate(-16.9285, 14.7960)
            );
            u1.setGeofence(geoU1);
            u2.setGeofence(geoU2);
            userRepository.saveAll(List.of(u1, u2));

            Animal primaryAnimalU1 = null;
            Animal primaryAnimalU2 = null;
            List<Animal> animalsU1 = new ArrayList<>();
            List<Animal> animalsU2 = new ArrayList<>();

            // 10 animals per user, 5 in troupeau, 5 not
            for (int i = 1; i <= 10; i++) {
                Animal a = new Animal();
                a.setUser(u1);
                a.setAge(2 + (i % 5));
                a.setSexe(i % 2 == 0 ? Sexe.MALE : Sexe.FEMALE);
                a.setTaille(1.1f + (i % 3) * 0.1f);
                a.setPoids(250f + i * 10);
                a.setStatut(Statut.VIVANT);
                a.setRole(roleForIndex(i));
                a.setEspece(i % 3 == 0 ? Espece.BOVIN : (i % 3 == 1 ? Espece.OVIN : Espece.CAPRIN));
                a.setNom(a.getEspece().name().toLowerCase() + "_n" + i);
                if (i <= 5) a.setTroupeau(t1);
                animalRepository.save(a);
                animalsU1.add(a);
                if (i == 1) {
                    primaryAnimalU1 = a;
                }
            }

            for (int i = 1; i <= 10; i++) {
                Animal a = new Animal();
                a.setUser(u2);
                a.setAge(3 + (i % 4));
                a.setSexe(i % 2 == 0 ? Sexe.MALE : Sexe.FEMALE);
                a.setTaille(1.0f + (i % 3) * 0.12f);
                a.setPoids(230f + i * 9);
                a.setStatut(Statut.VIVANT);
                a.setRole(roleForIndex(i + 1));
                a.setEspece(i % 3 == 0 ? Espece.CAPRIN : (i % 3 == 1 ? Espece.BOVIN : Espece.OVIN));
                a.setNom(a.getEspece().name().toLowerCase() + "_n" + i);
                if (i <= 5) a.setTroupeau(t2);
                animalRepository.save(a);
                animalsU2.add(a);
                if (i == 1) {
                    primaryAnimalU2 = a;
                }
            }

            // Create a device + one telemetry point per animal (for the map in the Flutter app).
            seedDevicesAndTelemetry(gf, u1, animalsU1, "1234567890123");
            seedDevicesAndTelemetry(gf, u2, animalsU2, "1234567890223");

            // Add a few animals without collars (no device, no telemetry).
            seedAnimalsWithoutCollar(u1, t1, animalsU1, 3, 100);
            seedAnimalsWithoutCollar(u2, t2, animalsU2, 2, 200);

            seedAlerts(u1, animalsU1);
            seedAlerts(u2, animalsU2);

            System.out.println("""
                    Mock data seeded:
                      - Users: 221770000001 / password, 221770000002 / password
                      - Admin: 221770000003 / password
                      - Animals: 10 per user + 5 without collars
                      - Devices+telemetry: 1 device + 1 point per animal (to make the map work in dev)
                      - Alerts: mixed types (open + resolved)
                    """);
        };
    }

    private Role roleForIndex(int i) {
        return switch (i % 3) {
            case 0 -> Role.REPRODUCTION;
            case 1 -> Role.LAITIERE;
            default -> Role.VIANDE;
        };
    }

    private void seedAlerts(User user, List<Animal> animals) {
        if (animals == null || animals.isEmpty()) return;

        Instant now = Instant.now();
        var picks = List.of(
                new AlertSeed(TypeAlerte.BATTERIELOW, false, now.minusSeconds(8 * 60), "Batterie faible sur %s"),
                new AlertSeed(TypeAlerte.GEOFENCE_EXIT, false, now.minusSeconds(25 * 60), "%s est sorti de la zone"),
                new AlertSeed(TypeAlerte.DEVICE_OFFLINE, false, now.minusSeconds(55 * 60), "Tracker hors ligne (%s)"),
                new AlertSeed(TypeAlerte.COLLAR_TAMPERING, true, now.minusSeconds(2 * 3600), "Suspicion de manipulation (%s)"),
                new AlertSeed(TypeAlerte.VOL, true, now.minusSeconds(26 * 3600), "Vol suspect (%s)"),
                new AlertSeed(TypeAlerte.DANGER, true, now.minusSeconds(3 * 24 * 3600), "SOS / Danger (%s)")
        );

        int limit = Math.min(animals.size(), picks.size());
        for (int i = 0; i < limit; i++) {
            Animal animal = animals.get(i);
            Device device = animal.getDevice();

            AlertSeed seed = picks.get(i);
            Alerte alert = new Alerte();
            alert.setTypeAlerte(seed.type());
            alert.setTs(seed.ts());
            alert.setResolved(seed.resolved());
            alert.setResolvedAt(seed.resolved() ? seed.ts().plusSeconds(15 * 60) : null);
            alert.setUser(user);
            alert.setAnimal(animal);
            alert.setDevice(device);
            alert.setMessage(seed.message().formatted(animal.getNom() != null ? animal.getNom() : animal.getEspece().name()));

            alerteRepository.save(alert);
        }
    }

    private record AlertSeed(TypeAlerte type, boolean resolved, Instant ts, String message) {}

    private void seedAnimalsWithoutCollar(User user, Troupeau troupeau, List<Animal> animalsOut, int count, int offset) {
        if (count <= 0) return;
        for (int i = 0; i < count; i++) {
            int idx = offset + i;
            Animal a = new Animal();
            a.setUser(user);
            a.setAge(1 + (idx % 6));
            a.setSexe(idx % 2 == 0 ? Sexe.MALE : Sexe.FEMALE);
            a.setTaille(1.0f + (idx % 3) * 0.1f);
            a.setPoids(200f + idx * 3);
            a.setStatut(Statut.VIVANT);
            a.setRole(roleForIndex(idx));
            a.setEspece(idx % 3 == 0 ? Espece.BOVIN : (idx % 3 == 1 ? Espece.OVIN : Espece.CAPRIN));
            a.setNom(a.getEspece().name().toLowerCase() + "_sans_collier_" + idx);
            a.setTroupeau(troupeau);
            animalRepository.save(a);
            if (animalsOut != null) {
                animalsOut.add(a);
            }
        }
    }

    private void seedDevicesAndTelemetry(GeometryFactory gf, User user, List<Animal> animals, String imeiPrefix13) {
        Random random = new Random(42);
        Point basePos = user.getPosition();
        if (user.getGeofence() != null && user.getGeofence().getZone() != null) {
            basePos = user.getGeofence().getZone().getCentroid();
            basePos.setSRID(4326);
        }
        if (basePos == null) {
            basePos = gf.createPoint(new Coordinate(-17.4677, 14.7167));
            basePos.setSRID(4326);
        }

        for (int i = 0; i < animals.size(); i++) {
            Animal animal = animals.get(i);

            Device device = new Device();
            device.setImei(imeiPrefix13 + String.format("%02d", i));
            device.setAnimal(animal);
            device.setStatusCollar(StatusCollar.ACTIF);
            deviceRepository.save(device);
            animal.setDevice(device);

            // Random-ish point around the user position.
            double lon = basePos.getX() + (random.nextDouble() - 0.5) * 0.01;
            double lat = basePos.getY() + (random.nextDouble() - 0.5) * 0.01;
            Point p = gf.createPoint(new Coordinate(lon, lat));
            p.setSRID(4326);

            Telemetry telemetry = new Telemetry();
            telemetry.setDevice(device);
            telemetry.setPosition(p);
            telemetry.setSpeed(random.nextDouble() * 2.0);
            telemetry.setBatteryLevel(30 + random.nextInt(70));
            telemetry.setGsmSignal(10 + random.nextInt(20));
            telemetry.setTransmissionStatus(com.gettgi.mvp.entity.enums.StatutTransmission.ENVOYE);
            telemetry.setStatusCollar(StatusCollar.ACTIF);
            telemetry.setTs(Instant.now().minusSeconds(random.nextInt(3600)));
            telemetryRepository.save(telemetry);
        }
    }

    private Geofence createGeofence(GeometryFactory gf, User user, String name, Coordinate... coordinates) {
        Coordinate[] coords = new Coordinate[coordinates.length + 1];
        System.arraycopy(coordinates, 0, coords, 0, coordinates.length);
        coords[coordinates.length] = coordinates[0];
        Polygon polygon = gf.createPolygon(coords);
        polygon.setSRID(4326);

        Geofence geofence = new Geofence();
        geofence.setNom(name);
        geofence.setZone(polygon);
        geofence.setRadiusMeters(null);
        geofence.setUser(user);
        return geofenceRepository.save(geofence);
    }
}

