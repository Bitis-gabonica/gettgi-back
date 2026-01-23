# GettGi Backend

Backend Spring Boot pour l'application GettGi - Système de suivi et gestion de troupeaux.

## Architecture

### Approche Code First

Ce projet utilise une approche **code first** pour la gestion du schéma de base de données :

- **Source de vérité unique** : Les entités JPA (Java) définissent le schéma de la base de données
- **Hibernate gère automatiquement** : Les modifications des entités sont appliquées automatiquement en développement
- **Pas de scripts SQL manuels** : Le schéma est généré à partir du code Java

### Gestion des Migrations

Les migrations sont gérées entièrement par **Hibernate** via les entités JPA :

1. **Développement** : Modifiez les entités JPA → Hibernate met à jour automatiquement le schéma
2. **Production** : Hibernate valide que le schéma correspond aux entités (sécurité)

#### Profils Spring

Le projet utilise des profils Spring pour différencier les environnements :

- **`dev`** (développement) :
  - `ddl-auto: update` - Hibernate met à jour automatiquement le schéma
  - `show-sql: true` - Les requêtes SQL sont affichées dans les logs
  - Profil actif par défaut

- **`prod`** (production) :
  - `ddl-auto: validate` - Hibernate vérifie seulement la cohérence
  - `show-sql: false` - Les requêtes SQL ne sont pas affichées (sécurité)
  - L'application échoue au démarrage si le schéma ne correspond pas

#### Configuration

Le profil actif est défini via la variable d'environnement `SPRING_PROFILES_ACTIVE` :

```bash
# Développement (par défaut)
SPRING_PROFILES_ACTIVE=dev

# Production
SPRING_PROFILES_ACTIVE=prod
```

### Activation PostGIS

L'extension PostGIS est activée automatiquement au démarrage de l'application par le composant `PostGisExtensionInitializer`, 
AVANT qu'Hibernate ne crée le schéma. Cela garantit que les types `geometry` sont disponibles lors de la création des tables.

### Index PostGIS GiST

Les index GiST PostGIS (nécessaires pour les requêtes spatiales) sont créés automatiquement par le composant `PostGisIndexInitializer` 
après la création du schéma :

- `idx_users_position_gist` sur `users.position`
- `idx_telemetry_position_gist` sur `telemetry.position`
- `idx_geofence_zone_gist` sur `geofence.zone`

Ces index ne peuvent pas être créés via les annotations JPA standard car ils nécessitent la syntaxe PostgreSQL spécifique `USING GIST`.

## Prérequis

- Java 17+
- Maven 3.9+
- Docker et Docker Compose
- PostgreSQL avec PostGIS (géré via Docker)

## Installation

1. **Cloner le repository**

2. **Configurer les variables d'environnement**

```bash
cp env.example .env
# Modifier les valeurs dans .env selon votre environnement
```

3. **Démarrer les services avec Docker Compose**

```bash
docker-compose up -d
```

Cela démarre :
- PostgreSQL avec PostGIS (port 5432)
- Redis (port 6379)
- Backend Spring Boot (port 2000)

## Développement

### Workflow de Migration

Pour modifier le schéma de la base de données :

1. **Modifier les entités JPA** dans `src/main/java/com/gettgi/mvp/entity/`
2. **Redémarrer l'application**
3. **Hibernate détecte les changements** et applique automatiquement les modifications :
   - Ajout de colonnes
   - Création de nouvelles tables
   - Modification de contraintes

**Note** : Hibernate `update` ne supprime pas les colonnes. Pour supprimer des colonnes, vous devrez :
- Utiliser un script SQL manuel, ou
- Utiliser `ddl-auto: create` (avec perte de données - uniquement en développement)

### Exemple de Modification

```java
@Entity
@Table(name = "animal")
public class Animal extends AuditableUuidEntity {
    
    // Ajout d'un nouveau champ
    @Column(length = 200)
    private String description;  // ← Nouveau champ
    
    // ... autres champs
}
```

Après redémarrage, Hibernate exécutera automatiquement :
```sql
ALTER TABLE animal ADD COLUMN description VARCHAR(200);
```

### Vérification du Schéma

Pour voir les requêtes SQL générées par Hibernate, vérifiez les logs de l'application (en mode `dev`, `show-sql: true`).

## Production

### Déploiement

1. **Configurer le profil production** :

```bash
SPRING_PROFILES_ACTIVE=prod
```

Le profil `prod` définit automatiquement `ddl-auto: validate` (pas besoin de le définir manuellement).

2. **S'assurer que le schéma est à jour** :

Le schéma doit être créé/mis à jour avant le déploiement. Hibernate validera que tout correspond aux entités.

3. **Premier déploiement** :

Pour le premier déploiement en production, vous pouvez temporairement utiliser `ddl-auto: create` pour créer le schéma initial, puis repasser à `validate` pour les déploiements suivants.

### Sécurité

- En production, `ddl-auto: validate` empêche toute modification automatique du schéma
- Les requêtes SQL ne sont pas affichées dans les logs (`show-sql: false`)
- L'application échoue au démarrage si le schéma ne correspond pas (sécurité)

## Structure du Projet

```
src/main/java/com/gettgi/mvp/
├── config/              # Configurations Spring
│   ├── PostGisExtensionInitializer.java  # Activation de l'extension PostGIS
│   ├── PostGisIndexInitializer.java  # Création automatique des index GiST
│   └── ...
├── entity/             # Entités JPA (définissent le schéma)
├── repository/          # Repositories Spring Data JPA
├── service/             # Services métier
└── controller/          # Contrôleurs REST

src/main/resources/
└── application.yaml          # Configuration principale avec profils dev/prod
```

## Variables d'Environnement Principales

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Profil Spring actif | `dev` |
| `SPRING_DATASOURCE_URL` | URL de connexion PostgreSQL | `jdbc:postgresql://localhost:5432/GettGi` |
| `SERVER_PORT` | Port du serveur | `2000` |

**Note** : `ddl-auto` est maintenant géré automatiquement par les profils Spring :
- Profil `dev` : `ddl-auto: update` (mise à jour automatique du schéma)
- Profil `prod` : `ddl-auto: validate` (validation uniquement)

Voir `env.example` pour la liste complète des variables.

## Technologies

- **Spring Boot 3.5.5**
- **Spring Data JPA / Hibernate**
- **PostgreSQL avec PostGIS** (requêtes spatiales)
- **Redis** (cache)
- **JWT** (authentification)
- **WebSocket** (communication temps réel)
- **MQTT** (télémétrie des colliers)

## Notes Importantes

### Activation PostGIS et Index GiST

L'extension PostGIS est activée automatiquement au démarrage par `PostGisExtensionInitializer` AVANT qu'Hibernate ne crée le schéma. 
Ce composant vérifie si PostGIS est déjà activé et l'active si nécessaire.

Les index GiST PostGIS sont ensuite créés automatiquement par `PostGisIndexInitializer`. Ce composant :

- S'exécute après l'initialisation de Hibernate
- Vérifie si les index existent déjà (idempotent)
- Crée les index uniquement s'ils n'existent pas

### Ancien Fichier SQL

Le fichier `db/V1__.sql.backup` est conservé à titre de référence historique mais n'est plus utilisé. Le schéma est maintenant géré entièrement par Hibernate.

## Support

Pour toute question ou problème, consultez la documentation Spring Boot ou contactez l'équipe de développement.
