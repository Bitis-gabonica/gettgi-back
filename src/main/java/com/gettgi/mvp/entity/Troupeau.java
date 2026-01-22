package com.gettgi.mvp.entity;

import com.gettgi.mvp.entity.AuditableUuidEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "troupeau", indexes = {
        @Index(name = "idx_troupeau_user", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_troupeau_user_nom", columnNames = {"user_id", "nom"})
})
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"animals", "user"})
public class Troupeau extends AuditableUuidEntity {

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nom;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "troupeau")
    private List<Animal> animals;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}