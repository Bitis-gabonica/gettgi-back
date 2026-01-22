package com.gettgi.mvp.entity;

import com.gettgi.mvp.entity.AuditableUuidEntity;
import com.gettgi.mvp.entity.enums.Espece;
import com.gettgi.mvp.entity.enums.Role;
import com.gettgi.mvp.entity.enums.Sexe;
import com.gettgi.mvp.entity.enums.Statut;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "animal", indexes = {
        @Index(name = "idx_animal_user", columnList = "user_id"),
        @Index(name = "idx_animal_troupeau", columnList = "troupeau_id"),
        @Index(name = "idx_animal_espece", columnList = "espece"),
        @Index(name = "idx_animal_statut", columnList = "statut")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "notifications", "troupeau", "device", "vaccins"})
public class Animal extends AuditableUuidEntity {


    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Sexe sexe;

    @Column(nullable = false)
    private Float taille;


    @Column(nullable = false)
    private Float poids;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Statut statut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 32)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Espece espece;

    @Column(nullable = true, length = 100)
    private String nom;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "troupeau_id")
    private Troupeau troupeau;

    @OneToOne(mappedBy = "animal", fetch = FetchType.LAZY)
    private Device device;

    @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vaccin> vaccins;

    @Column(name = "last_position", columnDefinition = "geometry(Point,4326)")
    private Point lastPosition;

    @Column(name = "last_position_ts")
    private Instant lastPositionTs;

    @Column(name = "last_position_inside_geofence")
    private Boolean lastPositionInsideGeofence;
}
