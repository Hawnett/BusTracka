# Prueba de caja negra: registrar el retraso de un viaje

**Responsable:** Alessandra Cosette Mañuico Jáuregui
**Historia de usuario:** HU 5.2, aviso de retraso del viaje
**Funcionalidad probada:** `PUT /api/admin/viajes/{id}/retraso` (pantalla Operaciones del administrador)
**Técnicas:** partición equivalente y análisis de valores límite
**Fecha de ejecución:** 16/11/2026, con `scripts/caja-negra-retraso.sh`
**Resultado:** 20 de 20 casos con el resultado esperado

## 1. Datos de entrada (5)

| # | Dato | Dónde viaja | Condición |
|---|---|---|---|
| 1 | `id` | URL | Número de un viaje que existe y que todavía no llega a su destino |
| 2 | `minutosRetraso` | cuerpo JSON | Entero obligatorio de 0 a 720 (12 horas). Con 0 el viaje vuelve a su hora |
| 3 | `motivo` | cuerpo JSON | Obligatorio: `TRAFICO`, `CLIMA`, `FALLA_MECANICA`, `ACCIDENTE` u `OTRO` |
| 4 | `detalle` | cuerpo JSON | Texto obligatorio de 10 a 200 caracteres que se muestra al pasajero |
| 5 | `avisarPasajeros` | cuerpo JSON | Booleano obligatorio: si es `true` y hay retraso, n8n envía el correo |

Precondición: sesión de **administrador** (`admin@bustracka.pe`).

## 2. Clases de equivalencia

| Dato | Clases válidas | Clases no válidas |
|---|---|---|
| `id` | (1) viaje existente y no finalizado | (2) no existe; (3) ya llegó a su destino; (4) no numérico |
| `minutosRetraso` | (5) entero de 0 a 720 | (6) menor que 0; (7) mayor que 720; (8) nulo; (9) decimal; (10) texto |
| `motivo` | (11) uno de los 5 valores | (12) nulo; (13) valor fuera de la lista |
| `detalle` | (14) de 10 a 200 caracteres | (15) vacío o solo espacios; (16) menos de 10; (17) más de 200 |
| `avisarPasajeros` | (18) `true` o `false` | (19) nulo; (20) no booleano |

## 3. Casos de prueba y resultados

Viaje 3 de `seed.sql` (sale mañana), viaje 10 (ya llegó). Cada caso no válido cambia un solo dato respecto al CP1.

| CP | Datos (lo que cambia respecto al CP1) | Clases | Esperado | Obtenido |
|---|---|---|---|---|
| 1 | id 3, 30 min, `TRAFICO`, "Congestion en la Panamericana Sur", `true` | (1)(5)(11)(14)(18) | 200, retraso guardado | 200 (pasa) |
| 2 | 0 min, `CLIMA`, detalle de 10 caracteres, `false` | límites inferiores | 200, vuelve a su hora, sin correo | 200 (pasa) |
| 3 | 720 min, `OTRO`, detalle de 200 caracteres | límites superiores | 200 | 200 (pasa) |
| 4 | id 999 | (2) | 404 El viaje no existe | 404 (pasa) |
| 5 | id 10 | (3) | 400 ya llegó a su destino | 400 (pasa) |
| 6 | id `abc` | (4) | 400 parámetro con formato inválido | 400 (pasa) |
| 7 | -1 min | (6) | 400 no puede ser negativo | 400 (pasa) |
| 8 | 721 min | (7) | 400 no puede superar 12 horas | 400 (pasa) |
| 9 | minutos nulo | (8) | 400 es obligatorio | 400 (pasa) |
| 10 | 15.5 min | (9) | 400 formato inválido | 400 (pasa) |
| 11 | `"abc"` en minutos | (10) | 400 formato inválido | 400 (pasa) |
| 12 | motivo nulo | (12) | 400 motivo es obligatorio | 400 (pasa) |
| 13 | motivo `LLUVIA` | (13) | 400 formato inválido | 400 (pasa) |
| 14 | detalle de solo espacios | (15) | 400 detalle es obligatorio | 400 (pasa) |
| 15 | detalle de 9 caracteres | (16) | 400 entre 10 y 200 caracteres | 400 (pasa) |
| 16 | detalle de 201 caracteres | (17) | 400 entre 10 y 200 caracteres | 400 (pasa) |
| 17 | avisarPasajeros nulo | (19) | 400 es obligatorio | 400 (pasa) |
| 18 | avisarPasajeros `"si"` | (20) | 400 formato inválido | 400 (pasa) |
| 19 | sesión de pasajero | precondición | 403 solo administrador | 403 (pasa) |
| 20 | sin token | precondición | 401 falta el token | 401 (pasa) |

## 4. Valores límite

| Dato | Fuera (abajo) | Borde inferior | Borde superior | Fuera (arriba) |
|---|---|---|---|---|
| `minutosRetraso` (0 a 720) | -1 → CP7 | 0 → CP2 | 720 → CP3 | 721 → CP8 |
| `detalle` (10 a 200 caracteres) | 9 → CP15 | 10 → CP2 | 200 → CP3 | 201 → CP16 |

## 5. Defectos encontrados en la primera ejecución (13/11/2026)

| ID | Caso | Problema | Corrección |
|---|---|---|---|
| D-05 | `"abc"` en minutos o en el id | Respondía 500 en vez de 400 | `GlobalExceptionHandler` maneja `HttpMessageNotReadableException` y `MethodArgumentTypeMismatchException` |
| D-06 | 15.5 minutos | Se guardaba truncado como 15 | `spring.jackson.deserialization.accept-float-as-int=false` |

Con esas correcciones, los 20 casos de la versión de 5 entradas pasan (ver `img/caja-negra-retraso.png`).