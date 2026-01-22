package com.gettgi.mvp.entity;

import com.gettgi.mvp.entity.enums.TypeAlerte;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Entity
@Table(name = "alerte", indexes = {
        @Index(name = "idx_alerte_type_ts", columnList = "type_alerte, ts"),
        @Index(name = "idx_alerte_user", columnList = "user_id"),
        @Index(name = "idx_alerte_animal", columnList = "animal_id"),
        @Index(name = "idx_alerte_device", columnList = "device_id"),
        @Index(name = "idx_alerte_resolved_at", columnList = "resolved_at")
})
@Getter
@Setter
@ToString(exclude = {"user", "animal", "device"})
public class Alerte extends AuditableUuidEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type_alerte", nullable = false, length = 64)
    private TypeAlerte typeAlerte;

    @Column(name = "ts", nullable = false)
    private Instant ts;

    @Column(name = "resolved", nullable = false)
    private boolean resolved = false;

    @Column(name = "message", length = 512)
    private String message;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "device_id")
    private Device device;
}

