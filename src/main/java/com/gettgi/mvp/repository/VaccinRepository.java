package com.gettgi.mvp.repository;

import com.gettgi.mvp.entity.Vaccin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VaccinRepository extends JpaRepository<Vaccin, UUID> {

    Page<Vaccin> findAllByAnimalId(Pageable pageable, UUID animalId);

    void deleteVaccinByIdAndAnimalId(UUID vaccinId, UUID animalId);
}
