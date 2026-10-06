# BusTracka - Sprint 1: Gestion de autenticacion y sesion

Arquitectura desacoplada: backend API REST en Java (Maven) + frontend en React (Vite + Bootstrap).

## Backend (`/backend`)

**Stack:** Spring Boot 3 (solo como servidor HTTP embebido y capa de controllers),
JDBC "a mano" para persistencia, PostgreSQL, JJWT.

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


## Levantar DB:

docker compose up -d


## Tener en cuenta previa a la ejecución en Java e uso del framework React

Ejecutar React + JS mediante los comando clásico de npm install, npm run dev

Ejecutar el backend utilizando IntelliJ Idea utilizando el archivo pom.xml

