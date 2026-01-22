package com.gettgi.mvp.repository;

import com.gettgi.mvp.entity.Telemetry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TelemetryRepository extends JpaRepository<Telemetry, UUID> {


    // Dernière position d’un device (temps réel)
    Optional<Telemetry> findTopByDevice_IdOrderByTsDesc(UUID deviceId);


    // Historique d’un device
    Page<Telemetry> findAllByDevice_IdAndTsBetween(UUID deviceId, Instant start, Instant end, Pageable pageable);

    // Dernières positions pour un lot de devices (pour carte multi-animaux)
    @Query("""
  SELECT t FROM Telemetry t
  WHERE t.id IN (
    SELECT t2.id FROM Telemetry t2
    WHERE t2.device.id IN :deviceIds
    AND t2.ts = (
      SELECT MAX(t3.ts) FROM Telemetry t3
      WHERE t3.device.id = t2.device.id
    )
  )
""")
    List<Telemetry> findLatestByDeviceIds(@Param("deviceIds") List<UUID> deviceIds);







}
