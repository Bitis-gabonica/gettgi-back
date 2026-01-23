package com.gettgi.mvp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Composant qui crée automatiquement les index GiST PostGIS nécessaires
 * pour optimiser les requêtes spatiales.
 * 
 * Ces index ne peuvent pas être créés via les annotations JPA standard
 * car ils nécessitent la syntaxe PostgreSQL spécifique "USING GIST".
 * 
 * Ce composant s'exécute après l'initialisation de Hibernate pour s'assurer
 * que les tables existent avant de créer les index.
 * 
 * NOTE: L'extension PostGIS doit être activée AVANT ce composant par
 * {@link PostGisExtensionInitializer}.
 */
@Component
@Slf4j
public class PostGisIndexInitializer {

    private final JdbcTemplate jdbcTemplate;

    public PostGisIndexInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Crée les index GiST PostGIS après l'initialisation du contexte Spring.
     * Utilise @Order pour s'assurer que Hibernate a terminé la création du schéma.
     */
    @EventListener(ContextRefreshedEvent.class)
    @Order(1000) // S'exécute après la plupart des autres listeners
    public void createPostGisIndexes() {
        log.info("Initialisation des index GiST PostGIS...");
        
        try {
            // Index GiST pour users.position (Point)
            createGistIndexIfNotExists(
                "idx_users_position_gist",
                "users",
                "position",
                "Index GiST pour optimiser les requêtes spatiales sur la position des utilisateurs"
            );

            // Index GiST pour telemetry.position (Point)
            createGistIndexIfNotExists(
                "idx_telemetry_position_gist",
                "telemetry",
                "position",
                "Index GiST pour optimiser les requêtes spatiales sur la position des télémétries"
            );

            // Index GiST pour geofence.zone (Polygon)
            createGistIndexIfNotExists(
                "idx_geofence_zone_gist",
                "geofence",
                "zone",
                "Index GiST pour optimiser les requêtes spatiales sur les zones géographiques"
            );

            log.info("Initialisation des index GiST PostGIS terminée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la création des index GiST PostGIS", e);
            // Ne pas faire échouer le démarrage de l'application si les index existent déjà
            // ou si les tables n'existent pas encore (cas d'une première initialisation)
        }
    }

    /**
     * Crée un index GiST s'il n'existe pas déjà.
     * 
     * @param indexName Nom de l'index
     * @param tableName Nom de la table
     * @param columnName Nom de la colonne géométrique
     * @param description Description de l'index (pour les logs)
     */
    private void createGistIndexIfNotExists(String indexName, String tableName, String columnName, String description) {
        try {
            // Vérifier si l'index existe déjà
            String checkIndexSql = """
                SELECT COUNT(*) 
                FROM pg_indexes 
                WHERE indexname = ? AND tablename = ?
                """;
            
            Integer count = jdbcTemplate.queryForObject(checkIndexSql, Integer.class, indexName, tableName);
            
            if (count != null && count > 0) {
                log.debug("L'index {} existe déjà, aucune action nécessaire", indexName);
                return;
            }

            // Vérifier si la table existe
            String checkTableSql = """
                SELECT COUNT(*) 
                FROM information_schema.tables 
                WHERE table_schema = 'public' AND table_name = ?
                """;
            
            Integer tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class, tableName);
            
            if (tableCount == null || tableCount == 0) {
                log.warn("La table {} n'existe pas encore, l'index {} sera créé lors du prochain démarrage", 
                    tableName, indexName);
                return;
            }

            // Créer l'index GiST
            String createIndexSql = String.format(
                "CREATE INDEX IF NOT EXISTS %s ON %s USING GIST (%s)",
                indexName, tableName, columnName
            );
            
            jdbcTemplate.execute(createIndexSql);
            log.info("Index GiST créé : {} ({})", indexName, description);
            
        } catch (Exception e) {
            log.warn("Impossible de créer l'index {} : {}. Il sera créé lors du prochain démarrage si nécessaire.", 
                indexName, e.getMessage());
        }
    }
}
