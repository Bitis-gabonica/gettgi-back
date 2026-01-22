-- Activer PostGIS pour les types GEOMETRY utilisés plus bas.
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE TABLE alerte
(
    id          UUID                        NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    type_alerte VARCHAR(64)                 NOT NULL,
    ts          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    resolved_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_alerte PRIMARY KEY (id)
);

CREATE TABLE animal
(
    id          UUID                        NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    age         INTEGER                     NOT NULL,
    sexe        VARCHAR(16)                 NOT NULL,
    taille      FLOAT                       NOT NULL,
    poids       FLOAT                       NOT NULL,
    statut      VARCHAR(32)                 NOT NULL,
    role        VARCHAR(32)                 NOT NULL,
    espece      VARCHAR(32)                 NOT NULL,
    race        VARCHAR(100)                NOT NULL,
    user_id     UUID                        NOT NULL,
    troupeau_id UUID,
    CONSTRAINT pk_animal PRIMARY KEY (id)
);

CREATE TABLE device
(
    id               UUID                        NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    imei             VARCHAR(100)                NOT NULL,
    animal_id        UUID,
    activation_date  TIMESTAMP WITHOUT TIME ZONE,
    firmware_version VARCHAR(32),
    CONSTRAINT pk_device PRIMARY KEY (id)
);

CREATE TABLE geofence
(
    id         UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    nom        VARCHAR(100),
    zone       GEOMETRY(Polygon, 4326)     NOT NULL,
    user_id    UUID                        NOT NULL,
    CONSTRAINT pk_geofence PRIMARY KEY (id)
);

CREATE TABLE notification
(
    id         UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    animal_id  UUID                        NOT NULL,
    alerte_id  UUID                        NOT NULL,
    CONSTRAINT pk_notification PRIMARY KEY (id)
);

CREATE TABLE telemetry
(
    id                  UUID                        NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    device_id           UUID                        NOT NULL,
    position            GEOMETRY(Point, 4326)       NOT NULL,
    speed               DOUBLE PRECISION,
    accelx              DOUBLE PRECISION,
    accely              DOUBLE PRECISION,
    accelz              DOUBLE PRECISION,
    pressure            DOUBLE PRECISION,
    battery_level       INTEGER,
    gsm_signal          INTEGER,
    transmission_status VARCHAR(32)                 NOT NULL,
    status_collar       VARCHAR(32),
    ts                  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_telemetry PRIMARY KEY (id)
);

CREATE TABLE troupeau
(
    id         UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    nom        VARCHAR(100)                NOT NULL,
    user_id    UUID                        NOT NULL,
    CONSTRAINT pk_troupeau PRIMARY KEY (id)
);

CREATE TABLE users
(
    id                      UUID                        NOT NULL,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    nom                     VARCHAR(100)                NOT NULL,
    prenom                  VARCHAR(100)                NOT NULL,
    adresse                 VARCHAR(255)                NOT NULL,
    telephone               VARCHAR(32)                 NOT NULL,
    email                   VARCHAR(255),
    password_hash           VARCHAR(100)                NOT NULL,
    position                GEOMETRY(Point, 4326),
    veterinaire_referent_id UUID,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE vaccin
(
    id          UUID                        NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    type_vaccin VARCHAR(64)                 NOT NULL,
    date        date                        NOT NULL,
    animal_id   UUID                        NOT NULL,
    CONSTRAINT pk_vaccin PRIMARY KEY (id)
);

CREATE TABLE veterinaire
(
    id         UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    nom        VARCHAR(100)                NOT NULL,
    prenom     VARCHAR(100)                NOT NULL,
    adresse    VARCHAR(255)                NOT NULL,
    telephone  VARCHAR(32)                 NOT NULL,
    CONSTRAINT pk_veterinaire PRIMARY KEY (id)
);

ALTER TABLE device
    ADD CONSTRAINT uc_device_animal UNIQUE (animal_id);

ALTER TABLE device
    ADD CONSTRAINT uc_device_imei UNIQUE (imei);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_telephone UNIQUE (telephone);

ALTER TABLE veterinaire
    ADD CONSTRAINT uc_veterinaire_telephone UNIQUE (telephone);

ALTER TABLE troupeau
    ADD CONSTRAINT uk_troupeau_user_nom UNIQUE (user_id, nom);

ALTER TABLE vaccin
    ADD CONSTRAINT uk_vaccin_animal_type_date UNIQUE (animal_id, type_vaccin, date);

CREATE INDEX idx_alerte_resolved_at ON alerte (resolved_at);

CREATE INDEX idx_alerte_type_ts ON alerte (type_alerte, ts);

CREATE INDEX idx_animal_espece ON animal (espece);

CREATE INDEX idx_animal_role ON animal (role);

CREATE INDEX idx_animal_statut ON animal (statut);

CREATE UNIQUE INDEX idx_device_imei ON device (imei);

CREATE INDEX idx_telemetry_timestamp ON telemetry (ts);

CREATE UNIQUE INDEX idx_user_email ON users (email);

CREATE UNIQUE INDEX idx_user_telephone ON users (telephone);

CREATE UNIQUE INDEX idx_vet_telephone ON veterinaire (telephone);

-- Index spatiaux créés après la définition des tables.
CREATE INDEX IF NOT EXISTS idx_users_position_gist     ON users     USING GIST (position);
CREATE INDEX IF NOT EXISTS idx_telemetry_position_gist ON telemetry USING GIST (position);
CREATE INDEX IF NOT EXISTS idx_geofence_zone_gist      ON geofence  USING GIST (zone);

ALTER TABLE animal
    ADD CONSTRAINT FK_ANIMAL_ON_TROUPEAU FOREIGN KEY (troupeau_id) REFERENCES troupeau (id);

CREATE INDEX idx_animal_troupeau ON animal (troupeau_id);

ALTER TABLE animal
    ADD CONSTRAINT FK_ANIMAL_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_animal_user ON animal (user_id);

ALTER TABLE device
    ADD CONSTRAINT FK_DEVICE_ON_ANIMAL FOREIGN KEY (animal_id) REFERENCES animal (id);

CREATE INDEX idx_device_animal ON device (animal_id);

ALTER TABLE geofence
    ADD CONSTRAINT FK_GEOFENCE_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_geofence_user ON geofence (user_id);

ALTER TABLE notification
    ADD CONSTRAINT FK_NOTIFICATION_ON_ALERTE FOREIGN KEY (alerte_id) REFERENCES alerte (id);

CREATE INDEX idx_notification_alerte ON notification (alerte_id);

ALTER TABLE notification
    ADD CONSTRAINT FK_NOTIFICATION_ON_ANIMAL FOREIGN KEY (animal_id) REFERENCES animal (id);

CREATE INDEX idx_notification_animal ON notification (animal_id);

ALTER TABLE telemetry
    ADD CONSTRAINT FK_TELEMETRY_ON_DEVICE FOREIGN KEY (device_id) REFERENCES device (id);

CREATE INDEX idx_telemetry_device ON telemetry (device_id);

ALTER TABLE troupeau
    ADD CONSTRAINT FK_TROUPEAU_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_troupeau_user ON troupeau (user_id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_VETERINAIRE_REFERENT FOREIGN KEY (veterinaire_referent_id) REFERENCES veterinaire (id);

CREATE INDEX idx_user_vet ON users (veterinaire_referent_id);

ALTER TABLE vaccin
    ADD CONSTRAINT FK_VACCIN_ON_ANIMAL FOREIGN KEY (animal_id) REFERENCES animal (id);

CREATE INDEX idx_vaccin_animal ON vaccin (animal_id);
