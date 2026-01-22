package com.gettgi.mvp.repository;

import com.gettgi.mvp.entity.Alerte;
import com.gettgi.mvp.entity.enums.TypeAlerte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlerteRepository extends JpaRepository<Alerte, UUID> {




Page<Alerte> findAllByUserId(UUID userId, Pageable pageable);

Page<Alerte> findAllByUserIdAndResolved(UUID userId, boolean resolved, Pageable pageable);
    Page<Alerte> findAllByUserIdAndCreatedAtBetween(UUID userId, Instant start, Instant end, Pageable pageable);

    Page<Alerte> findAllByUserIdAndAnimal_Id(UUID userId, UUID animalId, Pageable pageable);
    Page<Alerte> findAllByUserIdAndDevice_Id(UUID userId, UUID deviceId, Pageable pageable);
    Page<Alerte> findAllByUserIdAndTypeAlerte(UUID userId, TypeAlerte type, Pageable pageable);

    List<Alerte> findByAnimal_IdAndResolvedFalse(UUID animalId);

    Optional<Alerte> findTopByAnimal_IdAndTypeAlerteAndResolvedFalse(UUID animalId, TypeAlerte typeAlerte);

    Page<Alerte> findAllByAnimal_IdAndTsBetweenOrderByTsDesc(UUID animalId, Instant start, Instant end, Pageable pageable);
}
