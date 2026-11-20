# Guía de defensa — Alvaro Diego Mera Incio (BusTracka)

Todas las referencias son `archivo:línea` del repo `Hawnett/BusTracka`. Abre el archivo en el
proyector y ve directo a la línea: eso es lo que da los puntos de "dominio del tema".

---

## 0. Qué te están calificando (rúbrica de exposición)

| Ítem | Puntos | Qué esperan ver |
|---|---|---|
| Presentación del **diseño e implementación de una funcionalidad (1 o más HU)** | 5 | Recorrer una HU de punta a punta: pantalla → servicio HTTP → controller → service → DAO → BD → respuesta → render |
| Exposición y dominio del tema | 2 | Que respondas sin leer: "¿dónde se valida eso?" → archivo y línea |
| Talleres individuales | 10 | Fuera de esta exposición |

**Consecuencia práctica:** no intentes contar las 20 HU. Expón **HU 2.1–2.3 (búsqueda de viajes)**
completa, porque es la única donde además tienes caja blanca, caja negra y pruebas unitarias a tu
nombre (cap. 10.3 del informe). Las demás las tienes listas abajo como respaldo si preguntan.

---

## 1. Tu apertura (30 segundos, memorízala)

> "Mi responsabilidad fue la **capa de presentación completa en React** (15 pantallas y 14 componentes
> en `frontend/src`), más el `AuthContext` y las rutas protegidas. Además, en el capítulo 10.3 me tocó
> probar la búsqueda de viajes: caja blanca de `ViajeServiceImpl.buscarViajes()` con V(G) = 8,
> caja negra de `GET /api/viajes` con 18 casos y 5 pruebas unitarias en `BuscarViajesTest`.
> Voy a exponer la HU 2.1 a 2.3 de punta a punta, porque ahí se ve la arquitectura de 3 capas y mis pruebas."

Evidencia del informe que te respalda: sección **2.4.5.2** (tu contribución individual) y **Tabla 98**
(alcance de pruebas por integrante).

---

## 2. La arquitectura en un minuto (dibújala en la pizarra antes de abrir código)

```
BuscarViajes.jsx  ──(1)──►  viajeService.js  ──(2) HTTP GET /api/viajes──►  JwtAuthFilter
                                                                                 │ (3) valida token
                                                                                 ▼
TarjetaViaje.jsx  ◄──(7)──  ViajeResponseDTO  ◄──(6)──  ViajeServiceImpl  ◄──(4)──  ViajeController
                                                             │ (5) reglas de negocio
                                                             ▼
                                                        ViajeDaoImpl ──► PostgreSQL
```

Frase clave: **"el frontend nunca habla con la base de datos ni conoce entidades; solo consume DTOs"**
(patrón DTO del README; `model/` nunca sale por la API).

---

## 3. HU 2.1 – 2.3 · Búsqueda de viajes, horarios y asientos — **TU GUION PRINCIPAL**

### 3.1 Criterios de aceptación (Tabla 63 del informe)

| HU | Criterio de aceptación | Dónde se cumple |
|---|---|---|
| 2.1 | Filtra viajes por origen, destino y fecha | `ViajeDaoImpl.java:44`, `:48`, `:52` |
| 2.2 | Muestra hora de salida y llegada estimada | `Viaje.java:13-19` + `TarjetaViaje.jsx:51,75` |
| 2.3 | Muestra placa, capacidad y asientos libres u ocupados | `TarjetaViaje.jsx:59,65` + `MapaAsientos.jsx:6` |

### 3.2 El recorrido del clic (muestra los archivos en este orden)

1. **`frontend/src/pages/BuscarViajes.jsx:62`** — el `<form onSubmit={enviar}>`.
   - Estado de los filtros: línea **14** (`origen`, `destino`, `fecha`, `pasajeros`, `franja`).
   - `enviar` (línea **38**) hace `preventDefault()` y llama a `buscar` (línea **19**).
   - El selector de pasajeros solo ofrece 1 a 6 (línea **108**) y el de franja los 3 valores del enum (líneas **117-122**).
   - `useEffect` (línea **31**): al montar, carga terminales y hace una búsqueda sin filtros.
   - Detalle fino para lucirte: línea **42**, `intercambiar()` voltea origen y destino; y la línea **14**
     lee `?origen=` de la URL, que es como llega el usuario desde la pantalla de Terminales.

2. **`frontend/src/api/viajeService.js:5`** — `buscarViajes()`. Di: *"los componentes nunca llaman a
   axios directamente"*. Los filtros vacíos se mandan como `undefined` (líneas **8-12**) para que axios
   no los ponga en la query string: por eso el backend los recibe como `null` y los ignora.

3. **`frontend/src/api/authService.js:18`** — el **interceptor** que agrega `Authorization: Bearer <token>`
   a toda petición. Y en la línea **27** el interceptor de respuesta: si vuelve 401, borra el token y
   manda al login. Esto es lo que hace que el CP18 de tu caja negra (sin token → 401) tenga sentido
   también en la interfaz.

4. **`backend/.../controller/ViajeController.java:24-32`** — `@GetMapping` con los 5 `@RequestParam
   (required = false)`. Punto clave: `LocalDate fecha` y `FranjaHoraria franja` los convierte **Spring**,
   no tu código. Por eso `fecha=2026-13-45`, `pasajeros=abc`, `origen=abc` o `franja=MADRUGADA`
   devuelven 400 **antes** de entrar al service (ver 3.5).

5. **`backend/.../service/impl/ViajeServiceImpl.java:30-43`** — **el método que probaste en caja blanca**:
   - línea **32**: `int cantidad = pasajeros == null ? 1 : pasajeros;` → el valor por omisión de 1 pasajero.
   - líneas **33-35**: regla de 1 a 6 pasajeros (`MAX_PASAJEROS_POR_BUSQUEDA`, constante en la línea **24**).
   - líneas **36-38**: origen distinto del destino.
   - líneas **39-41**: fecha no anterior a hoy, **comparada en hora de Lima** (`ZonaApp.ahora()`, `util/ZonaApp.java:13`).
   - línea **42**: delega al DAO y convierte a DTO con `aDto` (línea **85**).

6. **`backend/.../dao/impl/ViajeDaoImpl.java:40-65`** — SQL dinámico: solo se agregan los filtros que
   llegaron (`if` en las líneas **44, 48, 52, 57**), y la línea **62** exige asientos libres suficientes.
   - línea **23**: `salida_estimada = fecha_salida + minutos_retraso` → **el retraso del administrador
     se refleja en la búsqueda** (HU 5.2 enganchada con la tuya).
   - línea **30**: `capacidad - COUNT(detalle_venta)` = asientos disponibles (criterio de la HU 2.3).
   - línea **42** (`WHERE salida_estimada > ahora`): nunca aparecen viajes que ya salieron.

7. **`frontend/src/components/TarjetaViaje.jsx`** — el render del resultado:
   - línea **51**: hora de llegada, con `+1` si llega al día siguiente (`utils/formato.js:15`, `diasDespues`).
   - línea **59**: la placa; línea **65**: "N asientos libres" o "Sin asientos libres".
   - líneas **19-24**: la insignia de retraso.
   - línea **82**: el botón se deshabilita si no hay asientos (`sinAsientos`).

### 3.3 Caja blanca (informe 10.3.1): V(G) = 8

Memoriza esta frase: **"14 nodos, 20 aristas, 7 nodos predicado; V(G) = 20 − 14 + 2 = 8 y también P + 1 = 7 + 1 = 8"**.

Correspondencia nodo ↔ línea real del código (esto es lo que te pueden pedir en vivo):

| Nodos | Línea de `ViajeServiceImpl.java` |
|---|---|
| 1, 2, 3 (`pasajeros == null`, `cantidad = 1`, `cantidad = pasajeros`) | 32 |
| 4, 5, 6 (`< 1`, `> 6`, `throw`) | 33-35 |
| 7, 8, 9 (`origen != null`, `equals(destino)`, `throw`) | 36-38 |
| 10, 11, 12 (`fecha != null`, `isBefore(hoy)`, `throw`) | 39-41 |
| 13 (`return` DAO → DTO) | 42 |

Si preguntan por qué 7 predicados y no 4 `if`: **las condiciones compuestas con `||` y `&&` se
separan en un nodo por condición** (línea 33 tiene dos, la 36 tiene dos, la 39 tiene dos).
Los caminos C1, C2, C3, C5, C7 y C8 están automatizados en `BuscarViajesTest`; C4 y C6 se
comprobaron en la caja negra (CP5 y CP1).

### 3.4 Caja negra (informe 10.3.2): 18 casos, 5 entradas

- Script real y ejecutable: **`scripts/caja-negra-busqueda.sh`** (se loguea solo con
  `cliente@bustracka.pe`, calcula "mañana" y "ayer" en zona de Lima, y corre CP1 a CP18).
- Técnicas: **partición de equivalencia** (18 clases, Tabla 113) + **valores límite**:
  1 y 6 pasajeros (CP2, CP3) con sus vecinos 0 y 7 (CP12, CP13), y la fecha de ayer como vecina de hoy (CP10).
- Cada caso **cambia un solo dato respecto del CP1**: eso es lo que hace atribuible el resultado.
- Los 18 pasaron; evidencia en `Entregables/img/ejecucion_caja_negra_busqueda.png` (Figura 39) y
  verificación por interfaz en las Figuras 40 y 41.

### 3.5 La pregunta que más probablemente te hagan

> **"Si tú solo validas 3 reglas, ¿quién rechaza `pasajeros=abc` o `franja=MADRUGADA`?"**

Respuesta: *"Spring, al convertir el `@RequestParam` al tipo declarado. Lanza
`MethodArgumentTypeMismatchException` y la traduce a 400 el manejador global en
`exception/GlobalExceptionHandler.java:82-85`. Mi método nunca se ejecuta en esos casos; por eso en
la caja negra los clasifiqué como clases no válidas de formato (clases 3, 6, 9, 13, 14, 17 y 18)
y no como reglas de negocio."* El 401 del CP18 viene de `security/JwtAuthFilter.java`.

### 3.6 Pruebas unitarias (informe 10.3.3): 5 casos

Archivo: `backend/src/test/java/.../service/impl/BuscarViajesTest.java` (tu nombre está en el
comentario de la línea 32).

| Caso | Método | Línea | Qué demuestra |
|---|---|---|---|
| PU-01 | `buscaConLosCincoFiltrosValidos` | 56 | Camino feliz con los 5 filtros |
| PU-02 | `sinCantidadDePasajerosBuscaParaUnPasajero` | 66 | `verify` de que el DAO recibe `1` |
| PU-03 | `rechazaCeroOMasDeSeisPasajeros` | 75 | Límites 0 y 7 + `never()`: no se llama al DAO |
| PU-04 | `rechazaOrigenIgualAlDestino` | 82 | Compara el mensaje exacto de la excepción |
| PU-05 | `rechazaUnaFechaPasada` | 89 | Fecha de ayer + `never()` |

Por qué con Mockito: el `ViajeDao` se reemplaza por un doble (línea **42**) y la fábrica estática se
intercepta con `mockStatic(DaoFactory.class)` (líneas **43-47**), **así la prueba no necesita base de
datos y aísla las reglas del servicio**. Comando: `mvn test -Dtest=BuscarViajesTest` → 5 pruebas, 0 fallos.

---

## 4. Respaldo: tus otras HU, con su ubicación exacta

### HU 1.1 – 1.4 · Autenticación (Release 1, Sprint 1)

| HU | Pantalla tuya | Clave del código | Backend que consume |
|---|---|---|---|
| 1.1 Registrarse | `pages/Register.jsx` | confirmación de contraseña en el cliente (línea **31**); `iniciarSesion()` tras registrar (**45**) | `AuthServiceImpl.java:60` (BCrypt en la **69**), validaciones en `RegisterRequestDTO.java:15-54` |
| 1.2 Iniciar sesión | `pages/Login.jsx:24` | `handleSubmit` → `login()` → `iniciarSesion()` | `AuthServiceImpl.java:78` |
| 1.2 Sesión persistente | `context/AuthContext.jsx:19-32` | al montar, con token guardado llama a `GET /auth/me` y restaura la sesión | `AuthController.java:71` |
| 1.3 Recuperar contraseña | `pages/RecoverPassword.jsx` | **un solo componente con dos pasos** según `?token=` en la URL (línea **17**, `useSearchParams`) | `AuthServiceImpl.java:89` (token de 15 min) y `:109` |
| 1.4 Cerrar sesión | `context/AuthContext.jsx:46-53` | avisa al servidor y **borra el token aunque el servidor falle** (línea **49**) | `AuthController.java:63` |
| Rutas protegidas | `routes/RutaPrivada.jsx:16` | sin sesión → `<Navigate to="/login">`; con `cargando` muestra spinner para no expulsar al usuario mientras se valida el token | `JwtAuthFilter` |
| Rol admin | `routes/RutaAdmin.jsx:9` | solo `idRol === 1` | el backend **igual** valida el rol (comentario de la línea 6) |

Frase de diseño para lucirte: *"usé Context API + un hook `useAuth` en vez de Redux porque el estado
de sesión es solo usuario + token y no justificaba la dependencia"* — está documentado en
`AuthContext.jsx:7-12`.

### HU 3.1 – 3.3 · Seguimiento, recorrido y terminales

| HU | Dónde | Detalle que debes saber |
|---|---|---|
| 3.1 Ubicación del bus | `components/VistaSeguimiento.jsx:17-36` | **polling cada 3 s** (`INTERVALO_MS`, línea 6); el `return` de la línea 32 limpia el `setInterval` al desmontar (si no, fuga de memoria) |
| 3.1 Panel de datos | `components/PanelSeguimiento.jsx` | progreso % (**14**), retraso (**30**), etiqueta "Simulación ×60" (**36**), próxima parada (**59**) |
| 3.2 Recorrido en el mapa | `components/MapaSeguimiento.jsx` | Leaflet + OpenStreetMap sin API key (**38**); el mapa se crea **una sola vez** (**36-43**) y la ruta se redibuja solo al cambiar de viaje (**51**); `tramoRecorrido` (**15**) pinta en verde lo ya avanzado usando distancia haversine (**7**) |
| 3.2 Itinerario | `components/ParadasViaje.jsx:13` | estados PASO / ACTUAL / PRÓXIMA que calcula `tracking/CalculadoraItinerario.java:18` |
| 3.3 Terminales | `pages/Terminales.jsx` + `components/MapaTerminales.jsx` | filtro por ciudad y texto (**20-22**); los botones "Salir de aquí / Llegar aquí" (**75-76**) llevan a la búsqueda con el filtro ya puesto |
| Reutilización | `pages/Seguimiento.jsx:28` y `pages/SeguimientoPublico.jsx:14` | **el mismo `VistaSeguimiento` sirve al seguimiento privado (por id) y al público (por token)**: recibe la función de carga como prop. Esto es tu mejor argumento de diseño de componentes |

El `useCallback` de `Seguimiento.jsx:28` existe para que `VistaSeguimiento` no reinicie el polling en
cada render, solo cuando cambia el viaje elegido (la prop va en el array de dependencias del `useEffect`).

### HU 4.1 – 4.3 y 4.5 · Compra, pago, comprobante y enlace

| HU | Tu código | Backend |
|---|---|---|
| 4.1 Elegir asiento | `components/MapaAsientos.jsx:6` (estado libre/seleccionado/ocupado) y `pages/Compra.jsx:39-45` (`alternar`, tope de 6 en la línea **10**) | `VentaServiceImpl.java:191` `validarAsientos()`; la BD tiene `UNIQUE (id_viaje, id_asiento)` |
| 4.2 Pago | `pages/Compra.jsx:49-67` (`pagar`); quita espacios de la tarjeta (**58**) y **vuelve a cargar los asientos si hubo error** (**63**) | `VentaServiceImpl.java:112` cobra; **121-128** anula el cobro (`pasarelaPago.anular`, línea **126**) si falla el registro |
| 4.3 Comprobante y QR | `pages/Comprobante.jsx:133` (el QR en base64) y `pages/BoletoPublico.jsx:47` (página pública a la que lleva el QR) | el QR apunta a `/boleto/<código>`: `VentaServiceImpl.java:217` |
| 4.5 Enlace de seguimiento | `pages/Comprobante.jsx:139-148` ("Seguir mi bus" + "Compartir viaje") | `VentaServiceImpl.java:133` → `EnlaceSeguimientoServiceImpl.java:56` |

Si preguntan por la doble venta: *"la pantalla impide elegir un asiento ocupado, pero eso es solo
usabilidad; la regla la hacen cumplir el service y la restricción UNIQUE. Por eso al fallar la compra
yo recargo el mapa de asientos en `Compra.jsx:63`."* (Honestidad de capas = punto a favor.)

### HU 6.1 – 6.3 · Historial y comprobantes

- `pages/MisCompras.jsx`: pestañas por estado (**8-14**, **63-68**) y buscador por código o ruta (**28-30**),
  total gastado excluyendo cancelados (**31**), y tachado del total si está cancelado (**112**).
- `pages/DetalleCompra.jsx`: ruta, fecha, asientos y estado (**44-59**); botones condicionados al estado
  del pasaje (**83-102**).
- `pages/Comprobante.jsx:25-35` + `api/ventaService.js:12`: descarga del PDF **con el token**, creando
  un `Blob` y un `<a download>` — porque un enlace normal no lleva la cabecera `Authorization`.
- **Ojo, dilo así si preguntan por el retraso en "Mis compras":** en el listado el retraso se refleja en
  la **salida estimada ya corregida** (`MisCompras.jsx:105`); la leyenda explícita "retraso N min" está
  en `DetalleCompra.jsx:46-51` y en `Comprobante.jsx:94-96`.

### HU 7.1 – 7.3 · Compartir el viaje

- `pages/CompartirViaje.jsx`: genera el enlace (**21-31**), copia al portapapeles con respaldo a
  `window.prompt` si el navegador lo bloquea (**33-40**), y comparte por **WhatsApp (97), correo (100),
  SMS (103)** y la API nativa `navigator.share` (**48, 106**). El QR del enlace: línea **136**.
  Solo se habilita si el pasaje está PRÓXIMO o EN_CURSO (**46, 125**).
- `pages/SeguimientoPublico.jsx`: vista sin sesión; si el backend responde **410** muestra
  "Enlace inválido" (**20, 38-43**). Ese 410 sale de `EnlaceSeguimientoServiceImpl.java:89` →
  `EnlaceInvalidoException` → `GlobalExceptionHandler.java:52-54`. Esto es exactamente el criterio
  de aceptación de la HU 7.3.

### HU 5.2 y 8.3 · Operaciones (administrador)

- `pages/Operaciones.jsx`: tabla de viajes no finalizados (**95-109**), formulario con minutos 0-720 (**124**),
  motivo del enum (**6-12, 129**), detalle y casilla "avisar a los pasajeros" (**140**).
  El mensaje de confirmación distingue si habrá correo o no (**55-57**).
- `api/adminService.js:6` → `PUT /api/admin/viajes/{id}/retraso` → `AdminController.java:27` →
  `ViajeServiceImpl.java:69` (rechaza si el viaje ya llegó, **71-73**; con 0 minutos no avisa, **75**).

---

## 5. Preguntas difíciles y tus respuestas (con la línea a mano)

| Pregunta | Respuesta corta |
|---|---|
| "¿Por qué validar en el cliente y en el servidor?" | "El cliente da respuesta inmediata (`Register.jsx:31`), pero la regla real está en el servidor (`RegisterRequestDTO.java:23-28`), porque el cliente se puede saltar con curl. Mi caja negra lo prueba: pegué a la API directamente, sin pasar por la pantalla." |
| "¿Dónde está la lógica de negocio del frontend?" | "Casi nada: la capa de presentación solo arma filtros, muestra DTOs y controla estados de carga y error. Las reglas están en los `service` del backend. La única lógica propiamente del cliente es el tope de 6 asientos (`Compra.jsx:10`) y el cálculo del tramo recorrido en el mapa (`MapaSeguimiento.jsx:15`)." |
| "¿Qué pasa si se vence el token mientras navego?" | "El interceptor de respuesta (`authService.js:27-38`) detecta el 401, borra el token y redirige al login; las rutas `/auth/*` se excluyen para que el error de credenciales se muestre en el formulario." |
| "¿El GPS es real?" | "No, y está documentado: `SimuladorUbicacion` recorre la ruta a ×60, y la pantalla lo declara con la etiqueta 'Simulación ×60' (`PanelSeguimiento.jsx:36`). Cambiarlo por GPS real es implementar otra clase de `ProveedorUbicacion` (DIP)." |
| "¿Por qué el mapa no se vuelve a dibujar completo cada 3 s?" | "Porque guardo las referencias de Leaflet en `useRef` y en cada actualización solo muevo el marcador y la polilínea recorrida (`MapaSeguimiento.jsx:67-79`); redibujar todo parpadearía y perdería el zoom del usuario." |
| "¿Qué defecto encontraste tú?" | Sé honesto: "en mis 18 casos de la búsqueda **los 18 pasaron**, no se registraron defectos. Los defectos del proyecto (D-01, D-05, D-06) los encontraron mis compañeros en sus pruebas. Lo que sí corregí en pareja con Reaid fue el rechazo de las solicitudes OPTIONS en el filtro JWT, que impedía restaurar la sesión al refrescar (cap. 2.4.4 del informe)." |
| "¿Por qué `pasajeros` es un filtro de búsqueda?" | "Porque el criterio pide mostrar disponibilidad: el SQL exige `capacidad − vendidos >= pasajeros` (`ViajeDaoImpl.java:62`), así no ofrecemos un viaje donde no caben todos." |
| "¿Cuántas pruebas corren en total?" | "`mvn test` corre 44 pruebas unitarias; 5 son mías." |

---

## 6. Para la demo en vivo

```bash
cd backend && docker compose up -d
```

```bash
cd backend && mvn spring-boot:run
```

```bash
cd frontend && npm install && npm run dev
```

```bash
cd backend && mvn test -Dtest=BuscarViajesTest
```

```bash
bash scripts/caja-negra-busqueda.sh
```

Usuarios de prueba: `cliente@bustracka.pe` / `Cliente1234` y `admin@bustracka.pe` / `Admin1234`.
Tarjeta que **rechaza** el pago: cualquier número que termine en `0000`.

**Ruta de clics de 2 minutos:** login → buscar Lima→Ica (muestra placa, llegada, asientos libres) →
poner fecha de ayer y mostrar el mensaje de error (es tu CP10) → "Ver detalle y mapa" → elegir asientos
→ pagar → comprobante con QR → "Compartir viaje" → abrir el enlace en una ventana de incógnito
(demuestra la HU 7.3 sin sesión).

**Plan B si no levanta nada:** ten abiertas las Figuras 39, 40, 41 y 42 del informe (están en
`Entregables/img/`) y expón sobre el código estático. Nunca improvises un "debería funcionar".

---

## 7. No digas esto

- No digas que hiciste el backend: el backend de autenticación y venta es de Reaid, la BD y n8n de
  Alessandra. Tú hiciste la **capa de presentación completa** y las **pruebas de la búsqueda**. Decir
  bien los límites de tu trabajo suma en "contribución individual".
- No digas "el frontend valida que el asiento esté libre": lo **muestra**; la garantía es la restricción
  UNIQUE en la base de datos.
- No digas que el correo o el GPS son reales (n8n + Mailpit y simulador).
- No inventes un número de V(G): el tuyo es **8** (el de 9 es el de Reaid, el filtro JWT).

---

## 8. Recorrido de la app desplegada, pantalla por pantalla

En el VPS, Caddy sirve tres cosas en el mismo dominio (`deploy/Caddyfile`):

| URL | Qué responde |
|---|---|
| `/` y cualquier otra ruta | el frontend React (`try_files {path} /index.html`, por eso las rutas del router funcionan al recargar) |
| `/api/*` | `reverse_proxy backend:8080` (el backend Java) |
| `/mailpit/` | el buzón de correos de prueba — **ábrelo en la exposición**: es la evidencia visual de las HU 4.4, 5.1, 5.2 y 5.3 |

### Antes de la demo: con qué usuario entrar

| Usuario | Para qué sirve | Qué NO verás |
|---|---|---|
| `admin@bustracka.pe` / `Admin1234` | aparece el ítem **05 Operaciones** en la barra (`Navbar.jsx:11,14`) y entra a `/operaciones` | **no tiene compras** (`seed.sql:64-72` solo crea compras para el cliente), así que "Mis compras" sale vacío |
| `cliente@bustracka.pe` / `Cliente1234` | tiene 4 compras, una por cada estado (próximo, en curso, completado, cancelado) | no ve Operaciones |

**Consecuencia para tu exposición:** entra como **cliente** para las HU 4.x, 6.x y 7.x, y cambia a
**admin** solo para la HU 5.2 / 8.3. Si demuestras el historial con el admin te va a salir el estado
vacío de `MisCompras.jsx:48-57` y parecerá que no funciona.

### Mapa pantalla → código → HU

| Lo que ves en el navegador | URL | Archivo que lo dibuja | HU |
|---|---|---|---|
| Formulario de ingreso | `/login` | `pages/Login.jsx` (layout en `components/AuthLayout.jsx`) | 1.2 |
| "Crear cuenta" | `/registro` | `pages/Register.jsx` | 1.1 |
| "¿La olvidaste?" → pedir enlace | `/recuperar` | `pages/RecoverPassword.jsx:38` (`FormularioSolicitarEnlace`) | 1.3 |
| El enlace del correo → nueva contraseña | `/recuperar?token=...` | `pages/RecoverPassword.jsx:86` (`FormularioNuevaContrasena`) — **misma ruta, otro formulario**, decidido en la línea 17 | 1.3 |
| Panel de salidas + buscador de 5 filtros | `/` | `pages/BuscarViajes.jsx` | **2.1, 2.2, 2.3** |
| Cada tarjeta de resultado (placa, llegada, asientos libres, insignia de retraso) | `/` | `components/TarjetaViaje.jsx` | 2.2, 2.3 |
| "Ver detalle y mapa" | `/viajes/:id/detalle` | `pages/DetalleViaje.jsx` + `MapaSeguimiento.jsx` + `ParadasViaje.jsx` | 3.2 |
| Plano del bus en 3 pasos + pago | `/viajes/:id` | `pages/Compra.jsx` + `components/MapaAsientos.jsx` | 4.1, 4.2 |
| Boleto con QR y sello | `/comprobante/:id` | `pages/Comprobante.jsx` (QR en la línea 133) | 4.3, 6.2 |
| La página a la que lleva el QR si lo escaneas | `/boleto/:codigo` | `pages/BoletoPublico.jsx` — **sin sesión**, dice Válido / No válido | 4.3 |
| Historial con pestañas y buscador | `/compras` | `pages/MisCompras.jsx` | 6.1 |
| Detalle de una compra | `/compras/:id` | `pages/DetalleCompra.jsx` (el "Horario original" tachado es la línea 46-51) | 6.3 |
| "Compartir viaje": enlace, QR, WhatsApp / correo / SMS | `/compras/:id/compartir` | `pages/CompartirViaje.jsx` | 7.1, 7.2 |
| El enlace compartido, abierto en incógnito | `/seguimiento/:token` | `pages/SeguimientoPublico.jsx` (el 410 → "Enlace inválido", línea 20 y 38-43) | **7.3** |
| Mapa en vivo con progreso, velocidad y paradas | `/seguimiento` | `pages/Seguimiento.jsx` → `VistaSeguimiento.jsx` → `MapaSeguimiento.jsx` + `PanelSeguimiento.jsx` | 3.1, 3.2 |
| Lista y mapa de terminales | `/terminales` | `pages/Terminales.jsx` + `MapaTerminales.jsx` | 3.3 |
| Tabla de viajes + formulario de retraso (solo admin) | `/operaciones` | `pages/Operaciones.jsx`, protegida por `routes/RutaAdmin.jsx:9` | 5.2, 8.3 |
| El correo del retraso que llega al pasajero | `/mailpit/` | lo manda el flujo de n8n `backend/n8n/Notificaciones de viaje.json` | 5.2 |

### Tres cosas que puedes demostrar en vivo en la URL desplegada

1. **Que el filtro de fecha pasada lo rechaza el servidor, no la pantalla** (es tu CP10): pon una fecha
   anterior a hoy y aparece el mensaje del backend. El texto exacto sale de
   `ViajeServiceImpl.java:40` y viaja hasta la pantalla por `authService.js:45`
   (`extraerMensajeError`) → `BuscarViajes.jsx:147-152`.
2. **Que el retraso del admin se propaga a todo**: en `/operaciones` ponle 30 minutos a un viaje
   programado, vuelve a `/` y la misma tarjeta ya muestra la insignia "Retraso 30 min" y la hora
   corrida. Eso pasa porque la salida estimada se calcula en SQL (`ViajeDaoImpl.java:23`) y en el
   modelo (`Viaje.java:13`), no en el frontend.
3. **Que el enlace compartido no necesita sesión**: copia el enlace de `/compras/:id/compartir`,
   ábrelo en una ventana de incógnito y se ve el mapa. Es el criterio de aceptación de la HU 7.3.

También puedes abrir `/api/viajes` directamente en el navegador: responde **401** porque falta el
token (`JwtAuthFilter`). Es el CP18 de tu caja negra, demostrado en 5 segundos y sin terminal.
