package com.gettgi.mvp.entity;

import com.gettgi.mvp.entity.AuditableUuidEntity;
import com.gettgi.mvp.entity.enums.TypeVaccin;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "vaccin", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vaccin_animal_type_date", columnNames = {"animal_id", "type_vaccin", "date"})
}, indexes = {
        @Index(name = "idx_vaccin_animal", columnList = "animal_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vaccin extends AuditableUuidEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type_vaccin", nullable = false, length = 64)
    private TypeVaccin typeVaccin;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;
}