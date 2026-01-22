package com.gettgi.mvp.entity;

import com.gettgi.mvp.entity.AuditableUuidEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Polygon;

@Entity
@Table(name = "geofence", indexes = {
        @Index(name = "idx_geofence_user", columnList = "user_id")
})
@Getter
@Setter
@ToString(exclude = {"user"})
public class Geofence extends AuditableUuidEntity {

    @NotBlank
    @Column(name = "nom", length = 100)
    private String nom;

    @Column(name = "zone", columnDefinition = "geometry(Polygon,4326)", nullable = false)
    private Polygon zone;

    @Column(name = "radius_meters")
    private Double radiusMeters;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
