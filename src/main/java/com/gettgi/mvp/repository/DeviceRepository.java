package com.gettgi.mvp.repository;

import com.gettgi.mvp.entity.Device;
import com.gettgi.mvp.entity.enums.StatusCollar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {


    Optional<Device> findByImei(String imei);

    Optional<Device> findByAnimal_Id(UUID animalId);
    boolean existsByAnimal_Id(UUID animalId);
    boolean existsByImei(String imei);


    void deleteByAnimal_Id(UUID animalId);

    void deleteByImei(String imei);

}
