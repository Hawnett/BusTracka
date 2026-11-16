# Caja negra: registrar el retraso de un viaje (HU 5.2)

Cosette Mañuico Jáuregui. Fecha de ejecución: 13/11/2026.

**Se prueba:** `PUT /api/admin/viajes/{id}/retraso`, con cuerpo `{"minutosRetraso": n}`. El administrador
registra cuántos minutos se atrasa la salida; con ese dato el flujo de n8n envía el aviso de retraso (HU 5.2).

**Técnicas:** partición equivalente (dividir cada entrada en clases válidas y no válidas y elegir un caso por clase)
y valores límite (probar justo en el borde permitido y el valor inmediatamente fuera).

## 1. Entradas y clases de equivalencia

| Entrada | Clases válidas | Clases no válidas |
|---|---|---|
| Sesión (token JWT) | (1) token de un administrador | (2) sin token; (3) token mal formado; (4) token de un pasajero |
| `id` del viaje (ruta) | (5) viaje que existe y no ha terminado | (6) viaje que no existe; (7) viaje que ya terminó; (8) texto en vez de número |
| `minutosRetraso` (cuerpo) | (9) entero entre 0 y 720 | (10) menor que 0; (11) mayor que 720; (12) ausente (null); (13) texto; (14) decimal |

Valores límite de `minutosRetraso`: 0 y 720 (bordes válidos), -1 y 721 (justo afuera).

## 2. Casos de prueba y resultados

Datos de prueba: `seed.sql` (viaje 3 programado para mañana, viaje 10 ya terminado; usuarios
`admin@bustracka.pe` y `cliente@bustracka.pe`). Se ejecutan con `scripts/caja-negra-retraso.sh`.

| Caso | Sesión | id | Cuerpo | Clases | Esperado | Obtenido (antes de corregir) | Obtenido (después) |
|---|---|---|---|---|---|---|---|
| CP1 | admin | 3 | `30` | 1, 5, 9 | 200 y el viaje con 30 min de retraso | 200 | 200 |
| CP2 | admin | 3 | `0` | 9 (límite inferior) | 200 | 200 | 200 |
| CP3 | admin | 3 | `720` | 9 (límite superior) | 200 | 200 | 200 |
| CP4 | admin | 3 | `-1` | 10 (límite) | 400 "no puede ser negativo" | 400 | 400 |
| CP5 | admin | 3 | `721` | 11 (límite) | 400 "no puede superar 12 horas" | 400 | 400 |
| CP6 | admin | 3 | `{}` | 12 | 400 "must not be null" | 400 | 400 |
| CP7 | admin | 3 | `"abc"` | 13 | 400 formato inválido | **500 "Error inesperado"** | 400 |
| CP8 | admin | 3 | `15.5` | 14 | 400 formato inválido | **200 y guarda 15** | 400 |
| CP9 | admin | 999 | `30` | 6 | 404 "El viaje no existe" | 404 | 404 |
| CP10 | admin | 10 | `30` | 7 | 400 "ya llegó a su destino" | 400 | 400 |
| CP11 | admin | abc | `30` | 8 | 400 parámetro inválido | **500 "Error inesperado"** | 400 |
| CP12 | pasajero | 3 | `30` | 4 | 403 solo administrador | 403 | 403 |
| CP13 | sin token | 3 | `30` | 2 | 401 falta el token | 401 | 401 |
| CP14 | `xxx.yyy.zzz` | 3 | `30` | 3 | 401 token inválido | 401 | 401 |

Cada caso no válido cubre una sola clase no válida y deja las demás entradas válidas, para saber exactamente qué causó el error.

## 3. Defectos encontrados

| ID | Defecto | Causa | Corrección | Estado |
|---|---|---|---|---|
| D-05 | Un dato con tipo equivocado (CP7 `"abc"`, CP11 id `abc`) devolvía 500 en vez de 400 | `GlobalExceptionHandler` no manejaba `HttpMessageNotReadableException` ni `MethodArgumentTypeMismatchException`; caían en el manejador genérico de `Exception` | Se agregaron los dos manejadores, que responden 400 con un mensaje claro | Cerrado |
| D-06 | Un decimal (CP8 `15.5`) se aceptaba y se guardaba truncado como 15 minutos | Jackson convierte decimales a entero por defecto | `spring.jackson.deserialization.accept-float-as-int=false` en `application.properties` | Cerrado |

Después de la corrección, los 14 casos dan el resultado esperado y `mvn test` sigue en verde (48 pruebas).