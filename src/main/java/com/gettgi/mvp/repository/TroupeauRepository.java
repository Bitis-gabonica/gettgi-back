package com.gettgi.mvp.repository;

import com.gettgi.mvp.entity.Troupeau;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TroupeauRepository extends JpaRepository<Troupeau, UUID> {


    Page<Troupeau> findAllByUserId(UUID userId, Pageable pageable);
    Page<Troupeau> findAllByUserTelephone(String telephone, Pageable pageable);
    boolean existsByUserIdAndNomIgnoreCase(UUID userId, String nom);

    Optional<Troupeau> findByUserId(UUID userId);

    Optional<Troupeau> findByIdAndUserTelephone(UUID id, String telephone);



}
