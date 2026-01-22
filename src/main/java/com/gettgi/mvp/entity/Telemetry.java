package com.gettgi.mvp.entity;

import com.gettgi.mvp.entity.AuditableUuidEntity;
import com.gettgi.mvp.entity.enums.StatusCollar;
import com.gettgi.mvp.entity.enums.StatutTransmission;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.Instant;

@Entity
@Table(name = "telemetry", indexes = {
        @Index(name = "idx_telemetry_device", columnList = "device_id"),
        @Index(name = "idx_telemetry_ts", columnList = "ts"),
        @Index(name = "idx_telemetry_device_ts", columnList = "device_id, ts")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"device"})
public class Telemetry extends AuditableUuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "position", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point position;

    private Double speed;
    private Double accelX, accelY, accelZ;
    private Double pressure;


    private Integer batteryLevel;

    private Integer gsmSignal;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission_status", nullable = false, length = 32)
    private StatutTransmission transmissionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_collar", length = 32)
    private StatusCollar statusCollar;

    @Column(name = "ts", nullable = false)
    private Instant ts;
}
