-- Datos de prueba (Release 1). Se ejecuta despues de schema.sql.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Usuarios de prueba (hash BCrypt $2a generado por pgcrypto, compatible con Spring)
--   admin@bustracka.pe   / Admin1234
--   cliente@bustracka.pe / Cliente1234
INSERT INTO usuario (nombre_usuario, contrasena, email, estado, id_rol) VALUES
 ('admin',   crypt('Admin1234',   gen_salt('bf', 10)), 'admin@bustracka.pe',   'ACTIVO', 1),
 ('cliente', crypt('Cliente1234', gen_salt('bf', 10)), 'cliente@bustracka.pe', 'ACTIVO', 2)
ON CONFLICT (email) DO NOTHING;

INSERT INTO terminal (id_terminal, nombre, ciudad, latitud, longitud) VALUES
 (1, 'Terminal Plaza Norte', 'Lima',     -12.0060, -77.0590),
 (2, 'Terminal Ica',         'Ica',      -14.0680, -75.7290),
 (3, 'Terminal Trujillo',    'Trujillo',  -8.1160, -79.0300),
 (4, 'Terminal Arequipa',    'Arequipa', -16.4090, -71.5370);

-- duracion_min = duracion REAL aproximada del viaje
INSERT INTO ruta (id_ruta, id_terminal_origen, id_terminal_destino, duracion_min) VALUES
 (1, 1, 2, 240),   -- Lima -> Ica
 (2, 1, 3, 540),   -- Lima -> Trujillo
 (3, 1, 4, 1000),  -- Lima -> Arequipa
 (4, 3, 1, 540);   -- Trujillo -> Lima

INSERT INTO ruta_punto (id_ruta, orden, latitud, longitud) VALUES
 (1,1,-12.0060,-77.0590),(1,2,-12.5800,-76.7000),(1,3,-13.0500,-76.3500),(1,4,-13.4100,-76.1300),(1,5,-14.0680,-75.7290),
 (2,1,-12.0060,-77.0590),(2,2,-11.1000,-77.6100),(2,3,-10.0700,-78.1500),(2,4,-9.0700,-78.5900),(2,5,-8.1160,-79.0300),
 (3,1,-12.0060,-77.0590),(3,2,-13.4100,-76.1300),(3,3,-14.0680,-75.7290),(3,4,-15.5000,-74.9000),(3,5,-16.4090,-71.5370),
 (4,1,-8.1160,-79.0300),(4,2,-9.0700,-78.5900),(4,3,-10.0700,-78.1500),(4,4,-11.1000,-77.6100),(4,5,-12.0060,-77.0590);

INSERT INTO bus (id_bus, placa, capacidad) VALUES
 (1, 'ABC-123', 20), (2, 'DEF-456', 20), (3, 'GHI-789', 20), (4, 'JKL-012', 20);

INSERT INTO asiento (id_bus, numero)
SELECT b.id_bus, n FROM bus b, generate_series(1, 20) AS n;

-- Las fechas se guardan como hora local de Lima (sin zona), igual que las lee el backend.
-- Viajes relativos a la fecha actual para que la demo siempre tenga viajes futuros.
-- Los de "hace 1 hora" sirven para ver el seguimiento en movimiento (Sprint 3).
INSERT INTO viaje (id_ruta, id_bus, fecha_salida, precio) VALUES
 (1, 1, (now() AT TIME ZONE 'America/Lima') - interval '1 hour',  45.00),
 (2, 2, (now() AT TIME ZONE 'America/Lima') - interval '30 minutes', 80.00),
 (1, 1, date_trunc('day', (now() AT TIME ZONE 'America/Lima')) + interval '1 day 8 hours',  45.00),
 (1, 1, date_trunc('day', (now() AT TIME ZONE 'America/Lima')) + interval '1 day 20 hours', 45.00),
 (2, 2, date_trunc('day', (now() AT TIME ZONE 'America/Lima')) + interval '1 day 21 hours', 80.00),
 (3, 3, date_trunc('day', (now() AT TIME ZONE 'America/Lima')) + interval '2 days 18 hours', 120.00),
 (4, 4, date_trunc('day', (now() AT TIME ZONE 'America/Lima')) + interval '1 day 22 hours', 80.00),
 (1, 1, date_trunc('day', (now() AT TIME ZONE 'America/Lima')) + interval '2 days 8 hours',  45.00);
