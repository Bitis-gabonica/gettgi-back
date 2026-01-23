package com.gettgi.mvp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Composant qui active l'extension PostGIS dans la base de données PostgreSQL
 * AVANT que Hibernate ne crée les tables avec des colonnes de type geometry.
 * 
 * Ce composant s'exécute très tôt dans le cycle de vie de Spring pour s'assurer
 * que PostGIS est disponible avant la création du schéma par Hibernate.
 */
@Component
@Slf4j
public class PostGisExtensionInitializer {

    private final JdbcTemplate jdbcTemplate;

    public PostGisExtensionInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Active l'extension PostGIS au démarrage de l'application.
     * Utilise @PostConstruct pour s'exécuter avant Hibernate.
     */
    @PostConstruct
    @Order(-1000) // S'exécute très tôt, avant Hibernate
    public void enablePostGisExtension() {
        log.info("Vérification et activation de l'extension PostGIS...");
        
        try {
            // Vérifier si PostGIS est déjà activé
            String checkExtensionSql = """
                SELECT COUNT(*) 
                FROM pg_extension 
                WHERE extname = 'postgis'
                """;
            
            Integer count = jdbcTemplate.queryForObject(checkExtensionSql, Integer.class);
            
            if (count != null && count > 0) {
                log.info("L'extension PostGIS est déjà activée");
                return;
            }

            // Activer l'extension PostGIS
            log.info("Activation de l'extension PostGIS...");
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS postgis");
            log.info("Extension PostGIS activée avec succès");
            
        } catch (Exception e) {
            log.error("Erreur critique lors de l'activation de l'extension PostGIS", e);
            throw new RuntimeException(
                "Impossible d'activer PostGIS. " +
                "Vérifiez que l'image PostgreSQL contient PostGIS et que l'utilisateur a les droits nécessaires.", 
                e
            );
        }
    }
}
