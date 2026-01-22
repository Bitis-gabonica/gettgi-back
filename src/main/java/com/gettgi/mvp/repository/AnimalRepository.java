package com.gettgi.mvp.repository;

import com.gettgi.mvp.entity.Animal;
import com.gettgi.mvp.entity.enums.Espece;
import com.gettgi.mvp.entity.enums.Statut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

public interface AnimalRepository extends JpaRepository<Animal, UUID>,
        JpaSpecificationExecutor<Animal> {


    Page<Animal> findAllByUserTelephone(String userTelephone, Pageable pageable);

    Optional<Animal> findByDevice_Imei(String imei);

    Page<Animal> findAllByUserIdAndDeviceIsNotNull(UUID userId, Pageable pageable);
    Page<Animal> findAllByUserIdAndDeviceIsNull(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);



    void deleteByUserId(UUID userId);

    Optional<Animal> findByIdAndUserTelephone(UUID id, String userTelephone);

    List<Animal> findAllByTroupeauId(UUID troupeauId);

    @Query("""
            select distinct a from Animal a
            join fetch a.user u
            join fetch a.device d
            where a.lastPositionTs is not null
              and a.lastPositionTs < :cutoff
            """)
    List<Animal> findAnimalsWithLastTelemetryBefore(@Param("cutoff") Instant cutoff);






}
