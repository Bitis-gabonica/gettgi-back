package com.gettgi.mvp.entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import jakarta.persistence.EntityListeners;
import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@ToString
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableUuidEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;


    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}