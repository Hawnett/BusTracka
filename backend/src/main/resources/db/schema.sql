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
