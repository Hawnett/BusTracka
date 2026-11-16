# BusTracka - Release 1 y 2

Arquitectura desacoplada: backend API REST en Java (Maven) + frontend en React (Vite + Bootstrap).
Los correos los envía n8n (Mailpit los recibe en desarrollo).

## Qué incluye

| Release / Sprint | Funcionalidad |
|------------------|---------------|
| R1 · Sprint 1 | Registro, login, recuperación de contraseña por correo, cierre de sesión (también en el servidor) |
| R1 · Sprint 2 | Búsqueda de viajes, horarios de salida y llegada, información del bus y asientos |
| R1 · Sprint 3 | Seguimiento del bus en el mapa (Leaflet/OpenStreetMap), paradas, terminales |
| R1 · Sprint 4 | Compra, pago, comprobante con QR (lleva a la validación del boleto), correo con QR y enlace de seguimiento |
| R2 · Sprint 1 | Avisos por correo de salida, retraso o cambio de horario y llegada próxima (n8n); el administrador registra retrasos |
| R2 · Sprint 2 | Historial con filtros por estado (próximo, en curso, completado, cancelado), detalle del viaje y PDF del comprobante |
| R2 · Sprint 3 | Compartir el viaje: enlace temporal guardado en la BD, QR del enlace, WhatsApp/correo/SMS y vista del familiar sin sesión |
| R2 · Sprint 4 | Pool de conexiones, secretos por variables de entorno, rol administrador, pruebas unitarias y prueba de carga |

**Usuarios de prueba** (los crea `seed.sql`): `cliente@bustracka.pe` / `Cliente1234` (ya tiene compras
en los cuatro estados) y `admin@bustracka.pe` / `Admin1234` (ve la pantalla de Operaciones).
Tarjeta de prueba: cualquiera de 13-19 dígitos (las que terminan en `0000` simulan un pago rechazado).

**Primera vez / si cambia el esquema** hay que recrear la base (borra los datos de prueba):
`docker compose down -v && docker compose up -d` (desde `backend/`). El Release 2 agrega columnas y la
tabla `seguimiento`, así que una base creada antes no sirve.

### Qué es real y qué está simulado

| Pieza | Estado | Cómo se cambia por la real |
|-------|--------|----------------------------|
| Pasarela de pago | Simulada (`PasarelaPagoSimulada`) | Crear otra clase que implemente `PasarelaPago` |
| Correo | n8n por webhook (`notificacion.webhook-url`) y Mailpit como buzón de pruebas | Cambiar la credencial SMTP de n8n por un servidor real |
| Avisos de viaje | Flujo de n8n que revisa la BD cada minuto | - |
| GPS del bus | Simulado (`SimuladorUbicacion`, ruta acelerada x60) | Otra implementación de `ProveedorUbicacion` |
| Mapa | Leaflet + OpenStreetMap (sin API key) | Cambiar `MapaSeguimiento.jsx` |
| QR y PDF | Reales (ZXing y OpenPDF) | - |

Con la simulación x60 el bus avanza en el mapa más rápido que el horario: las horas de las paradas
siguen el itinerario real y la pantalla lo indica con la etiqueta "Simulación ×60".
Con `tracking.velocidad-simulada=1` todo va en tiempo real.

### Patrones
- **Singleton:** `ConexionDB` (administra un pool HikariCP), usado por todos los DAOs.
- **Factory:** `DaoFactory` crea los 7 DAOs (usuario, terminal, ruta, viaje, asiento, venta, enlace de seguimiento).
- **DTO:** toda entrada/salida HTTP; los modelos (`model/`) nunca salen por la API.
- **DIP:** los services dependen de interfaces (`*Dao`, `PasarelaPago`, `NotificacionService`, `GeneradorQr`,
  `GeneradorComprobante`, `ProveedorUbicacion`).
- **SRP / 3 capas:** Controller -> Service (interfaz + Impl) -> DAO (interfaz + Impl).
- **Transacción:** `VentaDaoImpl.registrar` guarda venta y asientos juntos; la restricción
  `UNIQUE (id_viaje, id_asiento)` evita vender dos veces el mismo asiento y, si pasa, se anula el cobro.

## Cómo correrlo

```bash
# 1. Base de datos, n8n y Mailpit
cd backend
docker compose up -d

# 2. Backend (http://localhost:8080)
mvn spring-boot:run

# 3. Frontend (http://localhost:5173)
cd frontend
npm install
npm run dev
```

### Configurar n8n (una sola vez)

Los flujos están en `backend/n8n/` y el contenedor los ve en `/import`:

```bash
docker exec bustracka-n8n n8n import:credentials --input=/import/credenciales.ejemplo.json
docker exec bustracka-n8n n8n import:workflow --input="/import/Notificaciones de viaje.json"
docker exec bustracka-n8n n8n import:workflow --input="/import/Correo de comprobante.json"
docker exec bustracka-n8n n8n publish:workflow --id=Td5CDjuknCy3qTHi
docker exec bustracka-n8n n8n publish:workflow --id=yaPhSo4h7jEUz83n
docker restart bustracka-n8n
```

En Git Bash anteponer `MSYS_NO_PATHCONV=1` a cada comando para que no convierta las rutas `/import`.
En versiones anteriores de n8n, en lugar de `publish:workflow` se usa `update:workflow --id=<id> --active=true`.

Los correos se ven en **http://localhost:8025** (Mailpit). El editor de n8n queda en http://localhost:5678.
Los botones de los avisos apuntan a `http://localhost:5173` (constante `cfg.url` del flujo de notificaciones).

### Variables de entorno (todas opcionales en desarrollo)

| Variable | Para qué |
|----------|----------|
| `JWT_SECRET` | Clave HS256 en Base64. Si no está, se genera una al arrancar y las sesiones se pierden al reiniciar |
| `DB_URL`, `DB_USUARIO`, `DB_PASSWORD` | Conexión a PostgreSQL (por defecto `127.0.0.1:5433`) |
| `FRONTEND_URL` | Base de los enlaces del QR, del seguimiento y de los correos |
| `CORS_ORIGINS` | Orígenes permitidos, separados por coma |
| `NOTIFICACION_WEBHOOK_URL` | Webhook de n8n (vacío = solo se registra en el log) |

Si en tu PC ya hay un PostgreSQL instalado en el puerto 5433, el backend se conecta a ese y falla:
usa `DB_URL=jdbc:postgresql://127.0.0.1:<otro puerto>/bustracka_db` o detén ese servicio.

## Backend (`/backend`)

**Stack:** Spring Boot 3 (solo servidor HTTP embebido y controllers), JDBC "a mano" con pool HikariCP,
PostgreSQL, JJWT, ZXing (QR) y OpenPDF (PDF).

```
config/        ConexionDB (Singleton), CorsConfig, FilterConfig
model/         entidades y estados (EstadoViaje, EstadoPasaje)
dto/           entrada/salida de la API
dao/ dao/impl/ acceso a datos (DaoFactory + 7 DAOs JDBC)
service/ service/impl/  reglas de negocio
controller/    Auth, Viaje, Terminal, Seguimiento, Venta, Boleto (publico), Admin
security/      JwtUtil, JwtAuthFilter, SesionesCerradas
tracking/      simulador de GPS, recorrido e itinerario de paradas
notificacion/  correo por webhook de n8n y plantilla HTML
pago/ qr/ comprobante/  pasarela simulada, QR y PDF
```

### Endpoints

Todos con `Authorization: Bearer <token>`, salvo `/api/auth/*` (excepto `me` y `logout`) y `/api/publico/*`.

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/auth/register` · `/login` | Registro e inicio de sesión (devuelven el JWT y el rol) |
| POST / PUT | `/api/auth/recover` | Envía el enlace de recuperación por correo / cambia la contraseña con el token |
| POST | `/api/auth/logout` | El servidor deja de aceptar el token |
| GET | `/api/auth/me` | Usuario del token |
| GET | `/api/terminales` | Terminales con dirección y coordenadas |
| GET | `/api/viajes?origen=&destino=&fecha=` | Viajes que aún no salen (con salida y llegada estimadas) |
| GET | `/api/viajes/en-curso` · `/{id}` · `/{id}/asientos` | Viajes en ruta, detalle y asientos |
| GET | `/api/seguimiento/{idViaje}` | Posición, progreso, paradas, próxima parada, llegada estimada |
| POST | `/api/ventas` | Compra: valida asientos, cobra, guarda, crea el enlace y envía el correo |
| GET | `/api/ventas` · `/{id}` · `/{id}/pdf` | Historial, comprobante y PDF |
| POST | `/api/ventas/{id}/enlace-seguimiento` | Enlace para compartir (y su QR) |
| GET | `/api/publico/seguimiento?token=` | Vista del familiar (410 si el enlace no vale) |
| GET | `/api/publico/boletos/{codigo}` | Validación del boleto (destino del QR) |
| GET / PUT | `/api/admin/viajes` · `/{id}/retraso` | Solo administrador: viajes y registro de retraso |

### Pruebas

```bash
cd backend
mvn test                          # 44 pruebas unitarias
node ../scripts/prueba-carga.mjs  # con el backend corriendo: 8 usuarios a la vez, <3 s (Definition of Done)
```

## Frontend (`/frontend`)

**Stack:** React 18 + Vite + React Router + Bootstrap 5 + Axios + Leaflet.

```
src/api/           llamadas HTTP (auth, viajes, ventas, seguimiento, admin)
src/context/       AuthContext (sesion y rol)
src/routes/        RutaPrivada, RutaAdmin
src/pages/         BuscarViajes, DetalleViaje, Compra, Comprobante, MisCompras, DetalleCompra,
                   CompartirViaje, Seguimiento, SeguimientoPublico, BoletoPublico, Terminales, Operaciones,
                   Login, Register, RecoverPassword
src/components/    mapa, panel de seguimiento, paradas, tarjetas, insignias de estado
```
