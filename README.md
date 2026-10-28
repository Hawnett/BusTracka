# BusTracka - Release 1 (Sprints 1 a 4)

Arquitectura desacoplada: backend API REST en Java (Maven) + frontend en React (Vite + Bootstrap).

## Que incluye el Release 1

| Sprint | Funcionalidad |
|--------|---------------|
| 1 | Registro, login, recuperacion de contrasena, sesion con JWT |
| 2 | Terminales, rutas, horarios (viajes) y mapa de asientos |
| 3 | Seguimiento del bus en un mapa (Leaflet/OpenStreetMap) y terminales |
| 4 | Compra de pasajes, pago, comprobante con QR, correo y enlace de seguimiento |

**Usuarios de prueba** (los crea `seed.sql`): `cliente@bustracka.pe` / `Cliente1234`
y `admin@bustracka.pe` / `Admin1234`. Tarjeta de prueba: cualquiera de 13-19 digitos
(las que terminan en `0000` simulan un pago rechazado).

**Primera vez / si cambia el esquema** hay que recrear la base:
`docker compose down -v && docker compose up -d` (desde `backend/`).

### Que es real y que esta simulado

| Pieza | Estado | Como se cambia por la real |
|-------|--------|----------------------------|
| Pasarela de pago | Simulada (`PasarelaPagoSimulada`) | Crear otra clase que implemente `PasarelaPago` |
| Correo (n8n) | Simulado en el log; opcional webhook (`notificacion.webhook-url`) | Apuntar el webhook al flujo de n8n |
| GPS del bus | Simulado (`SimuladorUbicacion`, ruta acelerada x60) | Otra implementacion de `ProveedorUbicacion` |
| Mapa | Leaflet + OpenStreetMap (sin API key) | Cambiar `MapaSeguimiento.jsx` |
| QR | Real (ZXing) | - |

### Patrones en el Release 1
- **Singleton:** `ConexionDB`, usado por todos los DAOs.
- **Factory:** `DaoFactory` crea los 6 DAOs (usuario, terminal, ruta, viaje, asiento, venta).
- **DTO:** toda entrada/salida HTTP; los modelos (`model/`) nunca salen por la API.
- **DIP:** los services dependen de interfaces (`*Dao`, `PasarelaPago`, `NotificacionService`, `GeneradorQr`, `ProveedorUbicacion`).
- **SRP / 3 capas:** Controller -> Service (interfaz + Impl) -> DAO (interfaz + Impl).
- **Transaccion:** `VentaDaoImpl.registrar` guarda venta y asientos juntos; la restriccion
  `UNIQUE (id_viaje, id_asiento)` evita vender dos veces el mismo asiento.

### Endpoints nuevos (todos con `Authorization: Bearer <token>`, salvo `/api/publico`)

| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| GET | `/api/terminales` | Lista de terminales |
| GET | `/api/viajes?origen=&destino=&fecha=` | Busca viajes futuros |
| GET | `/api/viajes/en-curso` | Viajes que ya salieron |
| GET | `/api/viajes/{id}` / `/{id}/asientos` | Detalle y asientos (libres/ocupados) |
| GET | `/api/seguimiento/{idViaje}` | Posicion actual del bus + ruta |
| GET | `/api/publico/seguimiento?token=` | Lo mismo, sin sesion (enlace del correo) |
| POST | `/api/ventas` | Compra: valida asientos, cobra, guarda, genera QR y correo |
| GET | `/api/ventas` / `/api/ventas/{id}` | Mis compras / comprobante |

## Backend (`/backend`)

**Stack:** Spring Boot 3 (solo como servidor HTTP embebido y capa de controllers),
JDBC "a mano" para persistencia, PostgreSQL, JJWT.

**Paquetes** (base `pe.edu.ulima.is2.bustracka`, siguiendo la convencion de los
repos de `ulima-software2`):

```
config/      ConexionDB (Singleton), CorsConfig, FilterConfig
model/       Usuario
dto/         LoginRequestDTO, RegisterRequestDTO, AuthResponseDTO,
             RecoverPasswordRequestDTO, ResetPasswordRequestDTO,
             UsuarioResponseDTO, ApiErrorResponseDTO
dao/         UsuarioDao (interfaz), DaoFactory (Factory)
dao/impl/    UsuarioDaoImpl (JDBC)
service/     AuthService (interfaz)
service/impl/AuthServiceImpl
controller/  AuthController
security/    JwtUtil, JwtAuthFilter
exception/   excepciones de negocio + GlobalExceptionHandler
```

**Patrones aplicados:** Singleton (`ConexionDB`), Factory (`DaoFactory`),
DTO (toda entrada/salida HTTP), SRP (cada clase con una responsabilidad) y
DIP (el service depende de la interfaz `UsuarioDao`, no de la implementacion).

### Como correrlo

1. Levantar PostgreSQL: `docker compose up -d` (crea la BD `bustracka_db` y
   ejecuta `schema.sql` automaticamente la primera vez).
2. Abrir la carpeta `backend` en IntelliJ como proyecto Maven.
3. Ejecutar `BusTrackaApplication` (o `mvn spring-boot:run`). Queda escuchando
   en `http://localhost:8080`.

### Endpoints

| Metodo | Ruta                | Descripcion                                   |
|--------|---------------------|------------------------------------------------|
| POST   | `/api/auth/register`| Registra un usuario nuevo                       |
| POST   | `/api/auth/login`   | Login, retorna `AuthResponseDTO` con el JWT     |
| POST   | `/api/auth/recover` | Solicita el enlace de recuperacion (simulado)   |
| PUT    | `/api/auth/recover` | Confirma la nueva contrasena con el token       |
| GET    | `/api/auth/me`      | Devuelve el usuario del token (ruta protegida)  |

## Frontend (`/frontend`)

**Stack:** React 18 + Vite + React Router + Bootstrap 5 + Axios.

```
src/api/authService.js       Centraliza las llamadas HTTP al backend
src/context/AuthContext.jsx  Context API + hook useAuth (estado global de sesion)
src/routes/RutaPrivada.jsx   Protege rutas que requieren sesion activa
src/pages/Login.jsx
src/pages/Register.jsx
src/pages/RecoverPassword.jsx
src/pages/Home.jsx           Pantalla minima post-login
```

### Como correrlo

```bash
cd frontend
npm install
npm run dev
```


## Correrlo

# Backend: levantar Postgres
cd backend
docker compose up -d

# Frontend
cd frontend
npm install
npm run dev
