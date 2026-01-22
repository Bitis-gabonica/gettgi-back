// src/main/java/com/gettgi/mvp/entity/Device.java
package com.gettgi.mvp.entity;

import com.gettgi.mvp.entity.enums.StatusCollar;
import jakarta.persistence.*;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "device", indexes = {
        @Index(name = "idx_device_imei", columnList = "imei", unique = true),
        @Index(name = "idx_device_animal", columnList = "animal_id")
}, uniqueConstraints = {
        @UniqueConstraint(name="uk_device_animal", columnNames = {"animal_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"animal", "telemetries"})
public class Device extends AuditableUuidEntity {


    @Column(nullable = false, unique = true, length = 15)
    private String imei;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "animal_id", unique = true)
    private Animal animal;

    private Instant activationDate;

    @Column(length = 32)
    private String firmwareVersion;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Telemetry> telemetries;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private StatusCollar statusCollar;


}
