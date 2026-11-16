# Compra de pasajes de BusTracka: diseño de caja negra

Fecha de revisión: 11/11/2026. Este documento contiene diseño; no se ejecutaron casos de prueba.

## Referencias y procedimiento de clase

Las páginas citadas son las posiciones de las páginas del PDF, contando la portada como página 1. Las rutas de clase son relativas a la carpeta de trabajo. No se consultó teoría externa.

| Referencia | Archivo y ubicación | Naturaleza y uso en este diseño |
|---|---|---|
| T9 | `Clases/Semana 9 - ISW2 2026-2 Aseguramiento de la Calidad del Software - v2.pdf`, pp. 65–66, 70, 72 y 74 | Teoría: comportamiento esperado desde requisitos o escenarios; caja negra como entradas y salidas. Enumera partición, límites, tablas de decisión y transiciones; sus ejemplos de clasificación no constituyen un procedimiento desarrollado para estas dos últimas. |
| T10 | `Clases/Semana 10 - ISW2 2026-2 Pruebas Unitarias.pdf`, pp. 37, 63–65 y 68 | Teoría y esquema: especificar entradas; determinar clases por rango, valor específico, conjunto o condición lógica; enumerarlas en tabla; elegir casos válidos y casos con una sola clase no válida y el resto válidas. |
| E10-B | Mismo PDF, pp. 66–73 | Ejemplo resuelto bancario: condiciones de entrada, tabla con clases numeradas entre paréntesis, casos válidos y no válidos con clases cubiertas. En pp. 72–73, el superíndice `c` señala la clase cubierta por primera vez. Se reproduce como `(n)ᶜ`. |
| T10-L | Mismo PDF, p. 74; T9, p. 74, ejemplo de cantidades 0, 1, 20 y 21 | Teoría de valores límite: complementar partición escogiendo bordes. Se usan extremos permitidos y valores inmediatamente exteriores, siguiendo también el ejemplo de cantidades de T9. |
| E10-E | Mismo PDF, ejercicio 3, pp. 98–104 | Ejercicio resuelto de empleados: especificación, análisis por campo, tabla de clases y casos con datos concretos y «Resultado Esperado». Se adopta esta columna para hacer ejecutables los casos. |
| T11 | `Clases/Semana 11 - ISW2 2026-2 Pruebas Integrales - v2.pdf`, pp. 5–10, 13–21 | Material de integración y dobles: contextualiza la diferencia con pruebas aisladas. No se transforma en una técnica de selección de caja negra. |

Se exploró recursivamente `Clases/`: se encontraron esos tres PDF y los dos proyectos de laboratorio. Los PDF pudieron leerse con `pdftotext` ya instalado. Las pp. 68 y 71–73 de T10 contienen esquemas/tablas que la extracción no recupera: se renderizaron y leyeron visualmente. No se atribuye lectura textual a las imágenes de código de otros ejercicios, que no se utilizan para derivar este diseño. No hubo un PDF completo ilegible; la lectura visual se concentró en las páginas relevantes para el procedimiento de caja negra.

En `Clases/Labs/2026-2.TallerMocks-main/2026-2.TallerMocks-main/` se revisaron `README.md`, `docs/reqs.md` (RF-02), `docs/especificacion.md` (§2, `processSale`), `docs/modelo.puml`, `pom.xml` y los fuentes, especialmente `SalesService.java` y `TestSalesService.java`. Enseñan ventas con stock, resultados y excepciones, aislamiento con Mockito y mutación con PIT. No proporcionan la tabla de partición de BusTracka ni autorizan trasladar reglas de videojuegos a pasajes.

En `Clases/Labs/PruebasUnitarias/PruebasUnitarias/` se revisaron `pom.xml`, `Main.java`, `Calculadora.java` y `TestCalculadora.java`: hay datos, resultado esperado y aserciones, incluida división por cero. No se extrae de ellos una metodología adicional de caja negra. Los binarios y reportes de `target/` no son evidencia de ejecución de este diseño.

Se aplican exclusivamente partición equivalente y análisis de valores límite. No se elaboran tablas de decisión, transiciones de estado, combinaciones exhaustivas, caminos internos ni complejidad ciclomática. Los apartados siguientes siguen el orden de T10 p. 68 y E10-E pp. 98–104; las referencias y precisiones acompañan los pasos para documentar su fundamento.

## 1. Especificar los datos de entrada

### Funcionalidad y versión revisada

Repositorio: `BusTracka/`. Rama: `feature/release2-cierre-brechas`. Commit: `24c59ab78ae791dcef7f6be1f3a738de8f04e0fc`. `git status --short` no reportó cambios al inicio. No se cambió de rama ni se modificó el proyecto.

Funcionalidad: pasajero autenticado selecciona un viaje, elige asientos, ingresa pasajero y tarjeta, confirma el pago y recibe el comprobante. El objeto de diseño es su comportamiento externo, con `POST /api/ventas` como frontera de entrada de los cinco datos; la pantalla forma parte del flujo y permite observar selección, total, mensajes y comprobante. La sesión válida es una precondición, no un sexto campo artificial.

La hoja `Rubrica`, celda `D18`, de `../Rubrica_Final_Proyecto_2026_2.xlsx` exige una funcionalidad con más de cuatro campos de entrada. El alcance completo tiene cinco propiedades reales en `CompraRequestDTO`. La pantalla de compra tiene tres entradas de texto y un selector de asientos; el viaje procede de una selección anterior y de la URL `/viajes/:id`. No se afirma que existan cinco cajas de texto en esa pantalla. Si la evaluación exige cinco controles editables simultáneamente, esa interpretación no queda satisfecha por esta pantalla y debe aclararse con el docente. No se cuentan precio, total, JWT, cada asiento de una lista ni campos de respuesta como entradas adicionales. La rúbrica también pide evidencias de ejecución: este diseño por sí solo no acredita esa parte.

### Requisito de compra y criterios disponibles

Fuente documental D1: `../BusTracka Documentacion R1 (2).docx`, versión 1.0, 11/10/2026, §§5.1–5.3 y 5.6. En esas secciones **HU-10** es compra o reserva de pasaje con asiento disponible; §5.3 exige selección disponible, QR y prevención de doble venta. §5.2 exige rechazar el asiento vendido para el mismo viaje y revertir el pago; §6.3, AC001, desarrolla el caso de compras concurrentes.

Hay contradicciones de identificación: §§8.1 y 8.1.2.1 llaman **HU-07** a compra (ESC-02) y **HU-10** a seguimiento; §11.2 también asocia HU-10 con seguimiento. Se conserva **HU-10 (compra, según §§5.1–5.3 y 5.6)** en este diseño y no se presenta HU-07 como una historia adicional de compra. La documentación corresponde a R1 y dice que R2 no se inició, mientras el README actual describe R1 y R2 y ubica compra en R1/Sprint 4. No se toma esa planificación como una versión coherente entre ambas fuentes.

Fuente D2: `BusTracka/README.md`, «Qué incluye», «Usuarios de prueba», «Qué es real y qué está simulado», «Endpoints»: compra y pago, comprobante con QR, tarjeta de 13–19 dígitos y rechazo simulado de terminación `0000`. Esta fuente actual documenta reglas que el informe R1 no detalla. No incorpora CVV, vencimiento ni Luhn.

| Dato real | Tipo / origen | Condición de entrada observada | Sustento del proyecto |
|---|---|---|---|
| `idViaje` | `Long`; viaje elegido antes de entrar a compra | Obligatorio; debe existir y tener salida estimada posterior al momento de compra | P1, P2; D1 refiere compra de un viaje, sin especificar nulidad ni límite temporal |
| `idAsientos` | `List<Long>`; selector múltiple | Lista obligatoria, de 1 a 6 asientos distintos considerados por la operación, pertenecientes al viaje y disponibles | P1, P2, P3; disponibilidad respaldada por D1; máximo 6 solo se especifica en pantalla/código |
| `nombrePasajero` | `String`; «Nombre completo» | No nulo, no vacío ni compuesto solo por espacios | P1; D1 no define formato ni longitud |
| `titularTarjeta` | `String`; «Titular de la tarjeta» | No nulo, no vacío ni compuesto solo por espacios | P1; D1/D2 no detallan formato ni longitud |
| `numeroTarjeta` | `String`; «Número de tarjeta» | Obligatorio, solo dígitos, longitud 13–19; si termina en `0000`, rechazo simulado | P1, P4; D2 documenta longitud y terminación |

Los identificadores no tienen un rango numérico de negocio documentado: se usa pertenencia/existencia, sin inventar un mínimo o un máximo. En tarjeta, longitud y contenido numérico son condiciones separadas, como las condiciones lógicas y de valor del ejemplo T10 p. 67.

### Correspondencia con la interfaz y API revisadas

| Ref. | Archivo, relativo a `BusTracka/` | Sección o elemento consultado y observación externa |
|---|---|---|
| P1 | `backend/src/main/java/pe/edu/ulima/is2/bustracka/dto/CompraRequestDTO.java` | Record completo: `@NotNull`, `@NotEmpty`, `@NotBlank` y patrón `\d{13,19}`. Existen exactamente los cinco campos revisados. |
| P2 | `backend/src/main/java/pe/edu/ulima/is2/bustracka/service/impl/VentaServiceImpl.java` | `comprar`, `validarAsientos`, `construirComprobante`: viaje existente/futuro, máximo 6, pertenencia y disponibilidad, total por asientos, rechazo de pago, comprobante. Se extraen condiciones y salidas; no se seleccionan caminos internos. |
| P3 | `frontend/src/pages/Compra.jsx`; `frontend/src/components/MapaAsientos.jsx` | `cargar`, `alternar`, `pagar` y formulario: máximo 6, ocupado deshabilitado, pagar deshabilitado sin selección, total calculado. Envía `Number(id)`, selección y tres textos. Elimina espacios de la tarjeta antes de enviar. |
| P4 | `backend/src/main/java/pe/edu/ulima/is2/bustracka/pago/PasarelaPagoSimulada.java` | `cobrar`: terminación `0000` rechazada; resto aprobado. `anular` solo registra una anulación simulada, sin movimiento bancario real. |
| P5 | `backend/src/main/java/pe/edu/ulima/is2/bustracka/controller/VentaController.java` | `@RequestMapping` y `comprar`: `POST /api/ventas`, validación del cuerpo y HTTP 201; usuario tomado de sesión. |
| P6 | `backend/src/main/java/pe/edu/ulima/is2/bustracka/exception/GlobalExceptionHandler.java`; `dto/ApiErrorResponseDTO.java` en el mismo paquete base | Validación y regla de negocio: 400; viaje no encontrado: 404; asiento vendido: 409; pago rechazado: 402. Cuerpo con `timestamp`, `status`, `error`, `message`. |
| P7 | `frontend/src/api/ventaService.js`; `frontend/src/api/authService.js`; `frontend/src/pages/Comprobante.jsx`; `frontend/src/App.jsx` | `comprar`, `llamar`, `extraerMensajeError`, página de comprobante y ruta protegida: error de API visible; éxito navega a `/comprobante/{idVenta}` y muestra confirmación y QR. |
| P8 | `frontend/src/pages/DetalleViaje.jsx`; `frontend/src/components/TarjetaViaje.jsx`; `backend/src/main/java/pe/edu/ulima/is2/bustracka/controller/ViajeController.java` | Selección previa y consulta de viaje/asientos. La pantalla de detalle ofrece elegir asientos para viaje programado con disponibilidad. |
| P9 | `backend/src/main/resources/db/seed.sql`; `schema.sql` | Datos de ejemplo, viajes relativos al día de carga, asientos y compra previa del asiento 5 del viaje 3. El esquema limita nombre de pasajero a 120 caracteres, pero no es una regla validada/documentada en la entrada. |
| P10 | `backend/src/main/java/pe/edu/ulima/is2/bustracka/dao/impl/VentaDaoImpl.java`; `dao/impl/AsientoDaoImpl.java` en el mismo paquete base | Confirmación de disponibilidad por viaje y de rechazo de doble venta. Solo se observarán comprobantes, listados y asientos desde la interfaz/API; no se inspeccionará la BD para decidir el resultado de un caso. |

El formulario tiene `noValidate`: los atributos `required` no autorizan a esperar bloqueo del navegador por texto vacío. El `inputMode` numérico tampoco garantiza dígitos. La validación observable procede de la API. El `maxLength={23}` cuenta caracteres escritos, incluidos espacios; el contrato recibido por API sigue siendo 13–19 dígitos. No se trata 23 como longitud válida de tarjeta.

## 2. Determinar las clases de equivalencia

Aplicación de T10 pp. 64–65, 67–68: se separa cada condición de entrada. Para cantidad y longitud se define un rango válido y dos clases exteriores; para presencia, disponibilidad y pertenencia se usa condición lógica o pertenencia a conjunto. Los nombres no tienen longitud de negocio especificada: únicamente se particiona si contienen texto no blanco.

«Válida» en la tabla significa que la condición permite completar la compra. Por ello una tarjeta de formato correcto con terminación `0000` pertenece a una clase no válida para **compra aprobada**, aunque sea una solicitud bien formada y su rechazo sea el resultado correcto. No se confunde rechazo previsto con defecto.

La cantidad se evalúa para listas de identificadores distintos. El sistema elimina duplicados antes de contar/cobrar; no se ha encontrado una exigencia documental para rechazarlos. No se inventa una regla de unicidad de entrada ni un caso adicional por ella.

## 3. Tabla de clases de equivalencia

Formato y numeración: T10 p. 68 y E10-B p. 71; se añade la condición de entrada explícita como en E10-E p. 103. Las condiciones de pertenencia, salida futura y disponibilidad solo se evalúan si sus datos previos existen.

| Dato de entrada | Condición de entrada | Clases válidas | Clases no válidas |
|---|---|---|---|
| `idViaje` | Lógica: informado | (1) No nulo | (2) Nulo |
| `idViaje` | Conjunto: viajes existentes | (3) Identifica un viaje existente | (4) Identificador inexistente |
| `idViaje` | Lógica: aún no sale | (5) Salida estimada posterior a la compra | (6) Salida estimada igual o anterior a la compra |
| `idAsientos` | Lógica: lista informada | (7) Lista no nula | (8) Lista nula |
| `idAsientos` | Rango de cantidad de asientos distintos | (9) De 1 a 6 inclusive | (10) 0; (11) Más de 6 |
| `idAsientos` | Conjunto: asientos del viaje | (12) Todos pertenecen al viaje | (13) Alguno no pertenece al viaje, incluido identificador inexistente |
| `idAsientos` | Lógica: disponibles en ese viaje | (14) Todos libres | (15) Alguno ya vendido |
| `nombrePasajero` | Lógica: texto no blanco | (16) Contiene texto no blanco | (17) Nulo, vacío o solo espacios |
| `titularTarjeta` | Lógica: texto no blanco | (18) Contiene texto no blanco | (19) Nulo, vacío o solo espacios |
| `numeroTarjeta` | Lógica: informado/no blanco | (20) Contiene texto no blanco | (21) Nulo, vacío o solo espacios |
| `numeroTarjeta` | Rango de longitud de cadena no blanca | (22) 13 a 19 caracteres inclusive | (23) Menos de 13; (24) Más de 19 |
| `numeroTarjeta` | Lógica: contenido numérico | (25) Solo dígitos | (26) Algún carácter no numérico |
| `numeroTarjeta` | Lógica: aprobación simulada, con formato válido | (27) No termina en `0000` | (28) Termina en `0000` |

Fuente de las condiciones: P1 para presencia/texto/formato; P2 para existencia, salida, cantidad, pertenencia y disponibilidad; P4 y D2 para aprobación. La clasificación es diseño derivado de esas condiciones, no una afirmación de que todas estén exigidas por D1.

## 4. Casos de prueba

### Datos para la posterior ejecución

Perfil concreto de referencia basado en P9, **no comprobado contra una base en ejecución**:

- Sesión de pasajero: `cliente@bustracka.pe`, contraseña de ejemplo `Cliente1234` de `seed.sql`; obtener una sesión válida antes de cada caso que lo necesite.
- Viaje `3`: Lima–Ica, bus `ABC-123`, S/ 45.00 por asiento, salida al día siguiente de cargar la semilla a las 08:00, con el asiento número 5 previamente vendido. Debe seguir teniendo salida futura al ejecutar.
- Para las tablas se fija este perfil de IDs: asientos números 1–7 del bus `ABC-123` con IDs 1–7 respectivamente; números 1, 2, 3, 4, 6 y 7 libres en viaje 3; número 5 vendido. Asiento ID 8, número 8, libre para probar siete selecciones. ID 21 pertenece a otro bus. Viaje `1` ya salió y utiliza el mismo bus. Viaje `999999` no existe.
- Los IDs de asientos los asigna una secuencia y el `SELECT` de la semilla no garantiza su orden. **No se afirma que esos IDs estén efectivamente asignados así**. Antes de ejecutar, contrastar el perfil con `GET /api/viajes/3`, `GET /api/viajes/3/asientos`, y los viajes/asientos alternativos. Si difieren, registrar los IDs reales y sustituirlos coherentemente en las filas y el cuerpo de solicitud; conservar números, estados, cantidades y precios descritos. Verificar también que `999999` no exista.
- Cada fila requiere el perfil inicial indicado: una compra exitosa ocupa asientos y no debe contaminar las siguientes filas. Usar datos de prueba equivalentes y disponibles antes de cada fila. No se ha restablecido ni recreado ninguna base de datos como parte de este trabajo. Si no se dispone de un viaje futuro y esos estados, la fila queda pendiente de preparar; no se debe forzar su ejecución con datos incompatibles.

Se fijan `nombrePasajero = "Ana Torres"` y `titularTarjeta = "Luis Perez"` como datos ordinarios. El proyecto no exige que pasajero y titular sean iguales.

Para ejecución por pantalla: iniciar sesión, elegir el viaje, abrir compra, seleccionar los números de asiento de la fila, ingresar los tres textos y pulsar Pagar cuando esté habilitado. Observar resumen, respuesta y comprobante/mensaje. Para entradas que la pantalla impide (listas nulas, séptimo asiento, viaje nulo/inexistente), la frontera es la solicitud de API; no se modifica el frontend para producirlas. No se prescribe instalar herramientas ni automatizar su envío.

Cuerpo completo de referencia para el caso 1, con sesión válida y `Content-Type: application/json`, dirigido a `POST /api/ventas`:

```json
{
  "idViaje": 3,
  "idAsientos": [1, 2],
  "nombrePasajero": "Ana Torres",
  "titularTarjeta": "Luis Perez",
  "numeroTarjeta": "4242424242424242"
}
```

En cada fila se envían sus cinco valores explícitos; `null` representa JSON nulo y `""` cadena vacía. En pantalla, los espacios de tarjeta se eliminan antes de formar la solicitud: los datos de las tablas son los valores recibidos por la API.

### Resultados esperados: requisitos frente a implementación

Los códigos siguientes solo abrevian resultados en las tablas; no son estados de ejecución.

| Resultado | Requerido por documentación o criterios | Esperado según implementación revisada, pendiente de comprobar |
|---|---|---|
| Compra aceptada | D1 §5.3: elegir asiento disponible y generar QR; evitar doble venta. D2: compra, pago y comprobante | HTTP 201; comprobante con `idVenta`, `codigoComprobante`, `estado = PAGADA`, `idViaje`, asientos/nombre y total `cantidad × precio`. `qrBase64` informado; en pantalla, «¡Compra confirmada!» y QR. Los asientos elegidos pasan a ocupados en el listado del viaje. Método de pago muestra últimos 4 dígitos; no fijar el valor aleatorio de códigos ni la fecha exacta. |
| Viaje nulo | D1/D2 no establecen respuesta exacta | HTTP 400; `message` identifica `idViaje`; sin nueva compra. |
| Viaje inexistente | D1 presupone un viaje seleccionable; no define HTTP ni texto | HTTP 404; `message = El viaje no existe`; sin nueva compra. |
| Viaje que ya salió | D1/D2 no precisan la regla temporal de compra ni su error | HTTP 400; `message = Este viaje ya salio, no se pueden comprar pasajes`; sin nueva compra. |
| Lista nula o vacía | D1 exige selección de asiento disponible; no define formato de error | HTTP 400; `message` identifica `idAsientos`; sin nueva compra. En pantalla, sin selección, total S/ 0.00 y Pagar deshabilitado. |
| Más de 6 asientos | D1/D2 no fijan máximo. Pantalla sí anuncia máximo 6 | API: HTTP 400, `message = Maximo 6 asientos por compra`, sin nueva compra. Pantalla: el séptimo no se agrega y el total permanece correspondiente a seis; no es posible enviar siete por selección ordinaria. |
| Asiento ajeno al viaje | D1 refiere asiento del viaje, sin texto/HTTP exactos | HTTP 400; para ID 21, `message = El asiento 21 no pertenece a este viaje`; sin nueva compra. |
| Asiento vendido | D1 §§5.2–5.3: rechazar doble venta y revertir pago cuando corresponda | HTTP 409; para el asiento número 5, `message = El asiento 5 ya fue vendido`; sin nueva compra ni cambio de asientos libres. En pantalla, ocupado deshabilitado. Este caso con venta previa no acredita la carrera concurrente ni una reversión posterior al cobro. |
| Pasajero sin texto | D1/D2 no detallan esta validación | HTTP 400; `message` identifica `nombrePasajero`; sin nueva compra. |
| Titular sin texto | D1/D2 no detallan esta validación | HTTP 400; `message` identifica `titularTarjeta`; sin nueva compra. |
| Tarjeta sin texto | D2 exige tarjeta de 13–19 dígitos; no fija HTTP/texto exactos | HTTP 400; `message` identifica `numeroTarjeta`; sin nueva compra. Puede contener más de una infracción del mismo campo; no fijar orden ni traducción de mensajes por defecto. |
| Tarjeta de longitud/contenido incorrectos | D2: 13–19 dígitos | HTTP 400; `message` incluye `numeroTarjeta: debe tener entre 13 y 19 digitos`; sin nueva compra. |
| Pago rechazado | D2: terminación `0000` simula rechazo | HTTP 402; `message = Tarjeta rechazada por el emisor`; sin nueva compra, sin comprobante y sin ocupar los asientos libres elegidos. Pantalla muestra el mensaje y permanece en compra. |

En errores gestionados por P6 el cuerpo incluye `status`, `error`, `message` y `timestamp`; `status` debe coincidir con el HTTP. Los errores recibidos desde una compra enviada por pantalla se muestran mediante P7. Comprobar la ausencia de nueva compra mediante el listado externo `GET /api/ventas` y la disponibilidad mediante `GET /api/viajes/{id}/asientos`, comparados con sus valores anteriores; no inferir cobros reales de una pasarela simulada.

### Casos de prueba válidos

Selección según T10 p. 68 y E10-B p. 72: un caso ordinario cubre todas las clases válidas; se añaden dos elecciones de borde para T10-L. `(n)ᶜ` indica primera cobertura de la clase de este diseño, no aprobación de una prueba. Las filas 2 y 3 reutilizan clases pero aportan valores límite.

| Caso de Prueba | idViaje | idAsientos | nombrePasajero | titularTarjeta | numeroTarjeta | Clases cubiertas | Resultado Esperado |
|---|---|---|---|---|---|---|---|
| 1 | 3 | [1,2] | Ana Torres | Luis Perez | 4242424242424242 | (1)ᶜ (3)ᶜ (5)ᶜ (7)ᶜ (9)ᶜ (12)ᶜ (14)ᶜ (16)ᶜ (18)ᶜ (20)ᶜ (22)ᶜ (25)ᶜ (27)ᶜ | Compra aceptada; 2 asientos, total S/ 90.00; método terminado en 4242. |
| 2 | 3 | [1] | Ana Torres | Luis Perez | 1234567890123 | Mismas clases válidas del caso 1; bordes inferiores 1 asiento y 13 dígitos | Compra aceptada; 1 asiento, total S/ 45.00; método terminado en 0123. |
| 3 | 3 | [1,2,3,4,6,7] | Ana Torres | Luis Perez | 1234567890123456789 | Mismas clases válidas del caso 1; bordes superiores 6 asientos y 19 dígitos | Compra aceptada; 6 asientos, total S/ 270.00; método terminado en 6789. |

En estos tres casos se comprueba el resumen de cantidad y total antes de confirmar, y después los asientos, nombre y QR del comprobante. Son tres ejecuciones independientes con disponibilidad preparada, no tres compras sucesivas sobre los mismos asientos.

### Casos de prueba no válidos

Selección según T10 p. 68 y E10-B p. 73: se cambia una condición de un campo respecto al caso ordinario, sin combinar fallos independientes. En la columna de clases se registra la clase no válida que se incorpora; el resto conserva los valores válidos del caso 1. Para el caso 6, la cantidad no está definida porque la lista es nula; para viaje nulo/inexistente no se clasifica pertenencia a ese viaje. Para tarjeta nula no se clasifica longitud, dígitos ni terminación. Son condiciones dependientes no evaluables, no fallos adicionales inyectados. Se conserva el criterio práctico del ejemplo: un dato inválido por fila y los demás datos ordinarios.

| Caso de Prueba | idViaje | idAsientos | nombrePasajero | titularTarjeta | numeroTarjeta | Clase no válida cubierta | Resultado Esperado |
|---|---|---|---|---|---|---|---|
| 4 | null | [1,2] | Ana Torres | Luis Perez | 4242424242424242 | (2)ᶜ | Viaje nulo: HTTP 400, campo `idViaje`. Solo API. |
| 5 | 999999 | [1,2] | Ana Torres | Luis Perez | 4242424242424242 | (4)ᶜ | Viaje inexistente: HTTP 404, «El viaje no existe». Solo envío de compra por API; la pantalla no llega a cargar la compra de ese viaje. |
| 6 | 3 | null | Ana Torres | Luis Perez | 4242424242424242 | (8)ᶜ | Lista nula: HTTP 400, campo `idAsientos`. Solo API. |
| 7 | 3 | [] | Ana Torres | Luis Perez | 4242424242424242 | (10)ᶜ | Lista vacía: HTTP 400 por API; en pantalla, total S/ 0.00 y Pagar deshabilitado. |
| 8 | 3 | [1,2,3,4,6,7,8] | Ana Torres | Luis Perez | 4242424242424242 | (11)ᶜ | Más de 6: HTTP 400 por API. En pantalla, al intentar elegir los siete, solo se mantienen los seis primeros y total S/ 270.00. |
| 9 | 3 | [21] | Ana Torres | Luis Perez | 4242424242424242 | (13)ᶜ | Asiento ajeno al viaje: HTTP 400, «El asiento 21 no pertenece a este viaje». Solo API. |
| 10 | 3 | [5] | Ana Torres | Luis Perez | 4242424242424242 | (15)ᶜ | Asiento vendido: HTTP 409, «El asiento 5 ya fue vendido». En pantalla ese asiento está ocupado y no se puede elegir. |
| 11 | 3 | [1,2] | "" | Luis Perez | 4242424242424242 | (17)ᶜ | Pasajero sin texto: HTTP 400, campo `nombrePasajero`; en pantalla, mensaje recibido desde API. |
| 12 | 3 | [1,2] | Ana Torres | "" | 4242424242424242 | (19)ᶜ | Titular sin texto: HTTP 400, campo `titularTarjeta`; en pantalla, mensaje recibido desde API. |
| 13 | 3 | [1,2] | Ana Torres | Luis Perez | null | (21)ᶜ | Tarjeta sin texto: HTTP 400, campo `numeroTarjeta`. Nulo por API; el control de pantalla vacío representa la misma clase de falta de texto. |
| 14 | 3 | [1,2] | Ana Torres | Luis Perez | 123456789012 | (23)ᶜ | Tarjeta de 12 dígitos: HTTP 400, mensaje de patrón de tarjeta. |
| 15 | 3 | [1,2] | Ana Torres | Luis Perez | 12345678901234567890 | (24)ᶜ | Tarjeta de 20 dígitos: HTTP 400, mensaje de patrón de tarjeta. |
| 16 | 3 | [1,2] | Ana Torres | Luis Perez | 424242424242424A | (26)ᶜ | Tarjeta de 16 caracteres con letra: HTTP 400, mensaje de patrón de tarjeta. |
| 17 | 3 | [1,2] | Ana Torres | Luis Perez | 4242424242420000 | (28)ᶜ | Pago rechazado: HTTP 402; mensaje del emisor; los asientos 1 y 2 siguen libres y no aparece una nueva compra. |
| 18 | 1 | [1,2] | Ana Torres | Luis Perez | 4242424242424242 | (6)ᶜ | Viaje que ya salió: HTTP 400, «Este viaje ya salio, no se pueden comprar pasajes». La solicitud se envía por API. |

En caso 18, comprobar previamente que los asientos 1 y 2 del viaje 1 están libres; la ocupación se determina por viaje, no por el bus de forma global. En todos los rechazos rige «sin nueva compra» de la tabla de resultados, además del mensaje. Las variantes de nulo, vacío y espacios no se multiplican cuando representan la misma clase: se ha elegido un representante. Tampoco se combinan todos los asientos ni todos los textos.

### Análisis de valores límite

Complemento de partición, según T10 p. 74; representación de extremos y exteriores apoyada por T9 p. 74. No se generan filas de ejecución duplicadas: los casos anteriores ya contienen los valores seleccionados.

| Condición de entrada | Borde inferior / exterior | Borde superior / exterior | Casos ya seleccionados y resultado esperado |
|---|---|---|---|
| Cantidad de asientos: 1–6 | 0 / 1 | 6 / 7 | 7: cero rechazado por API; 2: uno aceptado; 3: seis aceptados; 8: siete rechazados por API y selección limitada a seis en pantalla. |
| Longitud de tarjeta: 13–19 dígitos | 12 / 13 | 19 / 20 | 14: doce rechazados; 2: trece aceptados; 3: diecinueve aceptados; 15: veinte rechazados. |

No se aplica límite numérico a IDs ni a terminación de tarjeta: la condición es de existencia o lógica, no un intervalo. Tampoco se inventa longitud máxima de titular o una restricción alfabética para nombres. Para la hora de salida, la clase (6) incluye igualdad, pero no se propone un caso cronometrado de igualdad exacta: faltan un instante controlable y una precisión de aceptación definida. El caso 18 representa un viaje que ya salió sin afirmar cobertura del borde temporal exacto.

### Registro para la ejecución posterior

Los ejemplos E10-B pp. 72–73 muestran clases cubiertas; E10-E p. 104 incorpora resultados esperados y no incluye una columna de resultado obtenido o estado. Se conserva ese formato. **Los 18 casos quedan pendientes de ejecución**; ningún superíndice, respuesta esperada o condición identificada equivale a un resultado obtenido. No hay capturas ni defectos confirmados.

## Precisiones y puntos pendientes de las fuentes

Estas precisiones delimitan la interpretación de las entradas y resultados anteriores; no agregan técnicas ni nuevos casos.

- D1 habla de comprar o reservar, pero la operación revisada realiza compra pagada y no expone reserva sin pago. Se diseña exclusivamente compra. Falta una especificación separada de reserva para poder diseñarla.
- Disponibilidad y QR tienen respaldo en HU-10 según §§5.1–5.3; máximo 6, obligatoriedad de ambos nombres y rechazo por salida pasada provienen de pantalla/API. El código ofrece un comportamiento actual para diseñar, pero no acredita que esas reglas sean requisitos aprobados. Los HTTP y textos exactos son expectativas de implementación salvo indicación documental expresa.
- `nombre_pasajero` tiene capacidad de 120 caracteres en el esquema; la pantalla y el DTO no declaran ese máximo. Falta decidir la regla de entrada y el error funcional requerido para textos mayores. No se convierte automáticamente un límite de almacenamiento en una validación funcional ni se inventan casos 120/121 con un error de negocio supuesto.
- No hay regla de igualdad entre pasajero y titular, formato alfabético estricto, CVV, vencimiento o Luhn. Tampoco hay requisito de rechazar IDs repetidos en la lista. No se agregan esas restricciones.
- D1 §5.2 y AC001 exigen prevención de doble venta y reversión en la carrera de compra. El caso 10 cubre la clase «ya vendido» de manera determinista. No demuestra por sí solo simultaneidad, reversión tras cobro ni el tiempo menor a un segundo de AC001. La pasarela simulada no expone una consulta externa de anulaciones/cobros: falta una salida observable que permita verificar esa reversión como caja negra. No se sustituye por inspección de llamadas privadas, mocks o logs internos ni se declara cumplido ese criterio.
- El informe R1 y el README actual difieren en historias/sprints y alcance. Debe corregirse la trazabilidad documental antes de tratar ambos como una única especificación. En este diseño el identificador coherente de compra es HU-10 según el backlog, con la contradicción de §8 explícita.
- Confirmar el perfil de viaje/asientos al ejecutar y disponer de estados independientes para las filas aceptadas; no se ha constatado la base de datos activa. Registrar entonces resultados obtenidos y evidencias si se requiere completar la rúbrica. Este entregable no los anticipa.