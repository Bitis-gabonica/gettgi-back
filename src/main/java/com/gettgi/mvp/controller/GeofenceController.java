package com.gettgi.mvp.controller;

import com.gettgi.mvp.dto.request.GeofenceUpsertRequestDto;
import com.gettgi.mvp.dto.response.GeofenceResponseDto;
import com.gettgi.mvp.dto.telemetry.GeoPointDto;
import com.gettgi.mvp.entity.Geofence;
import com.gettgi.mvp.entity.User;
import com.gettgi.mvp.repository.GeofenceRepository;
import com.gettgi.mvp.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/geofence")
@RequiredArgsConstructor
public class GeofenceController {

    private static final double EARTH_RADIUS_METERS = 6_371_000d;
    private static final int CIRCLE_POINTS = 64;

    private final UserRepository userRepository;
    private final GeofenceRepository geofenceRepository;
    private final GeometryFactory geometryFactory;

    @GetMapping
    public ResponseEntity<GeofenceResponseDto> getGeofence(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByTelephone(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UUID userId = user.getId();
        Geofence geofence = geofenceRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Geofence not found"));

        return ResponseEntity.ok(toDto(user, geofence));
    }

    @PutMapping
    public ResponseEntity<GeofenceResponseDto> upsertGeofence(
            @Valid @RequestBody GeofenceUpsertRequestDto request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByTelephone(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        double radiusMeters = request.radiusMeters();
        if (radiusMeters < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "radiusMeters must be >= 10");
        }

        Point centerPoint = toPoint(request.center());
        user.setPosition(centerPoint);
        userRepository.save(user);

        Geofence geofence = geofenceRepository.findByUser_Id(user.getId()).orElseGet(Geofence::new);
        geofence.setUser(user);
        geofence.setNom(request.name().trim());
        geofence.setRadiusMeters(radiusMeters);
        geofence.setZone(buildCirclePolygon(request.center(), radiusMeters));

        Geofence saved = geofenceRepository.save(geofence);
        return ResponseEntity.ok(toDto(user, saved));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteGeofence(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByTelephone(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        geofenceRepository.findByUser_Id(user.getId()).ifPresent(geofenceRepository::delete);
        return ResponseEntity.noContent().build();
    }

    private GeofenceResponseDto toDto(User user, Geofence geofence) {
        GeoPointDto center = null;
        if (geofence.getZone() != null) {
            Point centroid = geofence.getZone().getCentroid();
            centroid.setSRID(4326);
            center = new GeoPointDto(centroid.getY(), centroid.getX());
        } else if (user.getPosition() != null) {
            center = new GeoPointDto(user.getPosition().getY(), user.getPosition().getX());
        }

        Double radius = geofence.getRadiusMeters();
        if (radius == null && center != null && geofence.getZone() != null) {
            radius = approximateRadiusMeters(center, geofence.getZone());
        }

        GeoPointDto userPosition = null;
        if (user.getPosition() != null) {
            userPosition = new GeoPointDto(user.getPosition().getY(), user.getPosition().getX());
        } else if (center != null) {
            userPosition = center;
        }

        return new GeofenceResponseDto(
                geofence.getId(),
                geofence.getNom(),
                center,
                radius,
                userPosition
        );
    }

    private Point toPoint(GeoPointDto point) {
        Coordinate coordinate = new Coordinate(point.longitude(), point.latitude());
        Point p = geometryFactory.createPoint(coordinate);
        p.setSRID(4326);
        return p;
    }

    private Polygon buildCirclePolygon(GeoPointDto center, double radiusMeters) {
        double latRad = Math.toRadians(center.latitude());
        double lonRad = Math.toRadians(center.longitude());
        double angularDistance = radiusMeters / EARTH_RADIUS_METERS;

        List<Coordinate> coordinates = new ArrayList<>(CIRCLE_POINTS + 1);
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            double bearing = 2d * Math.PI * ((double) i / (double) CIRCLE_POINTS);
            double lat2 = Math.asin(
                    Math.sin(latRad) * Math.cos(angularDistance)
                            + Math.cos(latRad) * Math.sin(angularDistance) * Math.cos(bearing)
            );
            double lon2 = lonRad + Math.atan2(
                    Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(latRad),
                    Math.cos(angularDistance) - Math.sin(latRad) * Math.sin(lat2)
            );

            double latDeg = Math.toDegrees(lat2);
            double lonDeg = Math.toDegrees(lon2);
            coordinates.add(new Coordinate(lonDeg, latDeg));
        }

        if (!coordinates.isEmpty()) {
            coordinates.add(new Coordinate(coordinates.get(0)));
        }

        Polygon polygon = geometryFactory.createPolygon(coordinates.toArray(Coordinate[]::new));
        polygon.setSRID(4326);
        return polygon;
    }

    private double approximateRadiusMeters(GeoPointDto center, Polygon polygon) {
        if (polygon == null || polygon.getCoordinates() == null || polygon.getCoordinates().length == 0) {
            return 0d;
        }
        double max = 0d;
        for (Coordinate c : polygon.getCoordinates()) {
            if (c == null) continue;
            double d = haversineMeters(center.latitude(), center.longitude(), c.y, c.x);
            if (d > max) max = d;
        }
        return max;
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double rLat1 = Math.toRadians(lat1);
        double rLon1 = Math.toRadians(lon1);
        double rLat2 = Math.toRadians(lat2);
        double rLon2 = Math.toRadians(lon2);

        double dLat = rLat2 - rLat1;
        double dLon = rLon2 - rLon1;

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(rLat1) * Math.cos(rLat2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
