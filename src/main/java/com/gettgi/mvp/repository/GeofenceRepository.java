package com.gettgi.mvp.repository;

import com.gettgi.mvp.entity.Geofence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GeofenceRepository extends JpaRepository<Geofence, UUID> {


    Page<Geofence> findAllByUserId(UUID userId, Pageable pageable);
    
    Optional<Geofence> findByIdAndUserId(UUID id, UUID userId);

    Optional<Geofence> findByUser_Id(UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);


}
