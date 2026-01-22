package com.gettgi.mvp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gettgi.mvp.entity.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_telephone", columnList = "telephone", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"animals", "geofence", "troupeaux", "passwordHash"})
public class User extends AuditableUuidEntity {

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = true, length = 255)
    private String adresse;


    @Column(nullable = false, unique = true, length = 32)
    private String telephone;

    @Column(unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role;


    @Column(name = "password_hash", nullable = false, length = 100)
    @JsonIgnore
    private String passwordHash;

    @Column(name = "position", columnDefinition = "geometry(Point,4326)")
    private Point position;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Animal> animals ;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Geofence geofence ;

    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY)
    private List<Alerte> alertes ;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Troupeau> troupeaux ;


}
