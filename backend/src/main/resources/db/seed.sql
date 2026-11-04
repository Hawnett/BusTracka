-- Datos de prueba (Release 1 y 2). Se ejecuta despues de schema.sql.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Usuarios de prueba (hash BCrypt $2a generado por pgcrypto, compatible con Spring)
--   admin@bustracka.pe   / Admin1234   (rol ADMINISTRADOR: registra retrasos)
--   cliente@bustracka.pe / Cliente1234 (rol PASAJERO, ya tiene compras de ejemplo)
INSERT INTO usuario (nombre_usuario, contrasena, email, estado, id_rol) VALUES
 ('admin',   crypt('Admin1234',   gen_salt('bf', 10)), 'admin@bustracka.pe',   'ACTIVO', 1),
 ('cliente', crypt('Cliente1234', gen_salt('bf', 10)), 'cliente@bustracka.pe', 'ACTIVO', 2)
ON CONFLICT (email) DO NOTHING;

INSERT INTO terminal (id_terminal, nombre, ciudad, direccion, latitud, longitud) VALUES
 (1, 'Terminal Plaza Norte', 'Lima',     'Panamericana Norte s/n, Independencia', -12.0060, -77.0590),
 (2, 'Terminal Ica',         'Ica',      'Av. Municipalidad 305, Ica',            -14.0680, -75.7290),
 (3, 'Terminal Trujillo',    'Trujillo', 'Av. América Norte 2005, Trujillo',      -8.1160,  -79.0300),
 (4, 'Terminal Arequipa',    'Arequipa', 'Av. Arturo Ibáñez s/n, Cerro Colorado', -16.4090, -71.5370);

-- duracion_min = duracion REAL aproximada del viaje
INSERT INTO ruta (id_ruta, id_terminal_origen, id_terminal_destino, duracion_min) VALUES
 (1, 1, 2, 240),   -- Lima -> Ica
 (2, 1, 3, 540),   -- Lima -> Trujillo
 (3, 1, 4, 1000),  -- Lima -> Arequipa
 (4, 3, 1, 540),   -- Trujillo -> Lima
 (5, 4, 1, 1000);  -- Arequipa -> Lima

-- El primer y el ultimo punto son las terminales; los intermedios con nombre son paradas.
INSERT INTO ruta_punto (id_ruta, orden, nombre, latitud, longitud) VALUES
 (1,1,'Lima',-12.0060,-77.0590),(1,2,'Mala',-12.6580,-76.6320),(1,3,'Cañete',-13.0770,-76.3870),
 (1,4,'Chincha',-13.4180,-76.1320),(1,5,'Ica',-14.0680,-75.7290),
 (2,1,'Lima',-12.0060,-77.0590),(2,2,'Huacho',-11.1070,-77.6050),(2,3,'Huarmey',-10.0680,-78.1520),
 (2,4,'Chimbote',-9.0750,-78.5930),(2,5,'Trujillo',-8.1160,-79.0300),
 (3,1,'Lima',-12.0060,-77.0590),(3,2,'Chincha',-13.4180,-76.1320),(3,3,'Ica',-14.0680,-75.7290),
 (3,4,'Nazca',-14.8350,-74.9380),(3,5,'Camaná',-16.6240,-72.7110),(3,6,'Arequipa',-16.4090,-71.5370),
 (4,1,'Trujillo',-8.1160,-79.0300),(4,2,'Chimbote',-9.0750,-78.5930),(4,3,'Huarmey',-10.0680,-78.1520),
 (4,4,'Huacho',-11.1070,-77.6050),(4,5,'Lima',-12.0060,-77.0590),
 (5,1,'Arequipa',-16.4090,-71.5370),(5,2,'Camaná',-16.6240,-72.7110),(5,3,'Nazca',-14.8350,-74.9380),
 (5,4,'Ica',-14.0680,-75.7290),(5,5,'Chincha',-13.4180,-76.1320),(5,6,'Lima',-12.0060,-77.0590);

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

-- Un viaje largo en curso (id 9), uno que ya termino (id 10) y la vuelta desde Arequipa (id 11).
INSERT INTO viaje (id_ruta, id_bus, fecha_salida, precio) VALUES
 (3, 3, (now() AT TIME ZONE 'America/Lima') - interval '2 hours', 120.00),
 (1, 1, date_trunc('day', (now() AT TIME ZONE 'America/Lima')) - interval '3 days' + interval '9 hours', 45.00),
 (5, 3, date_trunc('day', (now() AT TIME ZONE 'America/Lima')) + interval '3 days 19 hours', 120.00);

-- Compras de ejemplo de cliente@bustracka.pe (una por cada estado del historial).
INSERT INTO venta (id_usuario, total, estado, codigo_pago, codigo_comprobante, tarjeta_ultimos4, fecha) VALUES
 ((SELECT id_usuario FROM usuario WHERE email = 'cliente@bustracka.pe'), 45.00, 'PAGADA', 'SIM-DEMO-0001', 'BT-DEMO000001', '4242',
  (now() AT TIME ZONE 'America/Lima') - interval '5 days'),
 ((SELECT id_usuario FROM usuario WHERE email = 'cliente@bustracka.pe'), 240.00, 'PAGADA', 'SIM-DEMO-0002', 'BT-DEMO000002', '4242',
  (now() AT TIME ZONE 'America/Lima') - interval '1 day'),
 ((SELECT id_usuario FROM usuario WHERE email = 'cliente@bustracka.pe'), 45.00, 'PAGADA', 'SIM-DEMO-0003', 'BT-DEMO000003', '4242',
  (now() AT TIME ZONE 'America/Lima') - interval '3 hours'),
 ((SELECT id_usuario FROM usuario WHERE email = 'cliente@bustracka.pe'), 45.00, 'ANULADA', 'SIM-DEMO-0004', 'BT-DEMO000004', '4242',
  (now() AT TIME ZONE 'America/Lima') - interval '6 days');

INSERT INTO detalle_venta (id_venta, id_viaje, id_asiento, nombre_pasajero, precio) VALUES
 ((SELECT id_venta FROM venta WHERE codigo_comprobante = 'BT-DEMO000001'), 10,
  (SELECT id_asiento FROM asiento WHERE id_bus = 1 AND numero = 7), 'Ana Torres', 45.00),
 ((SELECT id_venta FROM venta WHERE codigo_comprobante = 'BT-DEMO000002'), 9,
  (SELECT id_asiento FROM asiento WHERE id_bus = 3 AND numero = 11), 'Ana Torres', 120.00),
 ((SELECT id_venta FROM venta WHERE codigo_comprobante = 'BT-DEMO000002'), 9,
  (SELECT id_asiento FROM asiento WHERE id_bus = 3 AND numero = 12), 'Ana Torres', 120.00),
 ((SELECT id_venta FROM venta WHERE codigo_comprobante = 'BT-DEMO000003'), 3,
  (SELECT id_asiento FROM asiento WHERE id_bus = 1 AND numero = 5), 'Ana Torres', 45.00),
 ((SELECT id_venta FROM venta WHERE codigo_comprobante = 'BT-DEMO000004'), 10,
  (SELECT id_asiento FROM asiento WHERE id_bus = 1 AND numero = 8), 'Ana Torres', 45.00);
