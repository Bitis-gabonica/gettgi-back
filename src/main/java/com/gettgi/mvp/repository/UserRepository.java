package com.gettgi.mvp.repository;

import com.gettgi.mvp.entity.User;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Recherche par email (connexion)
     */
    Optional<User> findByEmail(String email);

    /**
     * Recherche par téléphone (principal au Sénégal)
     */
    Optional<User> findByTelephone(String telephone);


    // ========== VÉRIFICATIONS D'EXISTENCE ==========

    /**
     * Vérifier si un email existe déjà
     */
    boolean existsByEmail(String email);



    /**
     * Vérifier si un téléphone existe déjà
     */
    boolean existsByTelephone(String telephone);


}
