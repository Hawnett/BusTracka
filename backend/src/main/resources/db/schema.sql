-- Script de creacion de tablas para BusTracka - Sprint 1 (autenticacion y sesion)
-- Ejecutar contra la base "bustracka_db" levantada con docker-compose.yml

CREATE TABLE IF NOT EXISTS rol (
    id_rol      SERIAL PRIMARY KEY,
    nombre_rol  VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO rol (nombre_rol)
VALUES ('ADMINISTRADOR'), ('PASAJERO')
ON CONFLICT (nombre_rol) DO NOTHING;

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario      SERIAL PRIMARY KEY,
    nombre_usuario  VARCHAR(100)        NOT NULL,
    contrasena      VARCHAR(255)        NOT NULL, -- hash BCrypt, nunca texto plano
    email           VARCHAR(150)        NOT NULL UNIQUE,
    estado          VARCHAR(20)         NOT NULL DEFAULT 'ACTIVO', -- ACTIVO / INACTIVO
    id_rol          INTEGER             NOT NULL REFERENCES rol (id_rol),
    fecha_creacion  TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_usuario_email ON usuario (email);

-- ==========================================================
-- Release 1 - Sprints 2 a 4: rutas, viajes, asientos, ventas
-- ==========================================================

CREATE TABLE IF NOT EXISTS terminal (
    id_terminal  SERIAL PRIMARY KEY,
    nombre       VARCHAR(120) NOT NULL,
    ciudad       VARCHAR(80)  NOT NULL,
    latitud      DOUBLE PRECISION NOT NULL,
    longitud     DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS ruta (
    id_ruta               SERIAL PRIMARY KEY,
    id_terminal_origen    INTEGER NOT NULL REFERENCES terminal (id_terminal),
    id_terminal_destino   INTEGER NOT NULL REFERENCES terminal (id_terminal),
    duracion_min          INTEGER NOT NULL
);

-- Puntos (lat/lng) por los que pasa la ruta; los usa el seguimiento (Sprint 3)
CREATE TABLE IF NOT EXISTS ruta_punto (
    id_ruta_punto  SERIAL PRIMARY KEY,
    id_ruta        INTEGER NOT NULL REFERENCES ruta (id_ruta),
    orden          INTEGER NOT NULL,
    latitud        DOUBLE PRECISION NOT NULL,
    longitud       DOUBLE PRECISION NOT NULL,
    UNIQUE (id_ruta, orden)
);

CREATE TABLE IF NOT EXISTS bus (
    id_bus     SERIAL PRIMARY KEY,
    placa      VARCHAR(10) NOT NULL UNIQUE,
    capacidad  INTEGER     NOT NULL
);

CREATE TABLE IF NOT EXISTS asiento (
    id_asiento  SERIAL PRIMARY KEY,
    id_bus      INTEGER NOT NULL REFERENCES bus (id_bus),
    numero      INTEGER NOT NULL,
    UNIQUE (id_bus, numero)
);

CREATE TABLE IF NOT EXISTS viaje (
    id_viaje       SERIAL PRIMARY KEY,
    id_ruta        INTEGER        NOT NULL REFERENCES ruta (id_ruta),
    id_bus         INTEGER        NOT NULL REFERENCES bus (id_bus),
    fecha_salida   TIMESTAMP      NOT NULL,
    precio         NUMERIC(8, 2)  NOT NULL,
    minutos_retraso  INTEGER      NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_viaje_fecha ON viaje (fecha_salida);

CREATE TABLE IF NOT EXISTS venta (
    id_venta           SERIAL PRIMARY KEY,
    id_usuario         INTEGER       NOT NULL REFERENCES usuario (id_usuario),
    total              NUMERIC(10, 2) NOT NULL,
    estado             VARCHAR(20)   NOT NULL, -- PAGADA / ANULADA
    codigo_pago        VARCHAR(60)   NOT NULL, -- id de transaccion de la pasarela
    codigo_comprobante VARCHAR(40)   NOT NULL UNIQUE,
    fecha              TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- La restriccion UNIQUE (id_viaje, id_asiento) garantiza que un asiento no se
-- venda dos veces aunque dos usuarios compren al mismo tiempo.
CREATE TABLE IF NOT EXISTS detalle_venta (
    id_detalle        SERIAL PRIMARY KEY,
    id_venta          INTEGER       NOT NULL REFERENCES venta (id_venta),
    id_viaje          INTEGER       NOT NULL REFERENCES viaje (id_viaje),
    id_asiento        INTEGER       NOT NULL REFERENCES asiento (id_asiento),
    nombre_pasajero   VARCHAR(120)  NOT NULL,
    precio            NUMERIC(8, 2) NOT NULL,
    UNIQUE (id_viaje, id_asiento)
);

CREATE TABLE IF NOT EXISTS notificacion (
                                            id_notificacion  SERIAL PRIMARY KEY,
                                            id_usuario       INTEGER     NOT NULL REFERENCES usuario (id_usuario),
    id_viaje         INTEGER     NOT NULL REFERENCES viaje (id_viaje),
    tipo             VARCHAR(20) NOT NULL CHECK (tipo IN ('SALIDA', 'RETRASO', 'LLEGADA')),
    minutos_retraso  INTEGER     NOT NULL DEFAULT 0,
    estado           VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'ENVIADA')),
    fecha_creacion   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_envio      TIMESTAMP,
    UNIQUE (id_usuario, id_viaje, tipo, minutos_retraso)
    );
