# Pruebas unitarias de comprar() — BusTracka

Fecha de ejecución y actualización: 14/11/2026 (America/Lima). Estado: compilación y ejecución satisfactorias. Los 15 casos de comprar() fueron ejecutados y cumplieron sus aserciones; también pasó el caso conservado de validarBoleto(). La suite completa ejecutó 54 pruebas, sin fallos, errores ni omisiones.

## Versión y alcance

Repositorio de trabajo: `BusTracka/`, remoto `https://github.com/Hawnett/BusTracka.git`. Rama local: `feature/release2-cierre-brechas`. Commit de referencia: `24c59ab78ae791dcef7f6be1f3a738de8f04e0fc`. Al comenzar, `git -C BusTracka status --short` no reportó cambios. No se cambió de rama ni se actualizó el repositorio. No se hicieron commits, pushes ni merges.

Archivo de pruebas: `BusTracka/backend/src/test/java/pe/edu/ulima/is2/bustracka/service/impl/VentaServiceImplTest.java`. Contiene exactamente 16 métodos `@Test`: 15 sobre `comprar()` y el caso preexistente `elQrDeUnaCompraAnuladaNoEsValido`, de `validarBoleto()`, conservado literalmente y excluido del conteo de 15.

El objeto probado es una instancia real de `VentaServiceImpl`. No se sustituye `comprar()`, no se invocan métodos privados y no se arranca Spring, PostgreSQL, Docker, n8n ni servicios externos. La configuración de anotaciones del DTO corresponde a la frontera HTTP, no a estas llamadas directas al servicio.

## Materiales y procedimiento de clase

Se exploró recursivamente `Clases/`. Se encontraron tres PDF y los proyectos de laboratorio de calculadora y mocks. Se leyó el texto de los apartados relevantes con `pdftotext` ya instalado, sin consultar Internet. Páginas contadas desde la portada. Las páginas de código o diagramas insertados como imágenes no se presentan como ejemplos textuales revisados: el procedimiento usado se respalda en las páginas legibles y en los fuentes del laboratorio.

| Fuente | Página / sección | Aplicación |
|---|---|---|
| `Clases/Semana 10 - ISW2 2026-2 Pruebas Unitarias.pdf` | p. 7, Test Stub y Mock Object | Dobles que proporcionan respuestas controladas; sustitución de dependencias externas. |
| Mismo PDF | p. 14, ciclo de vida xUnit; pp. 24 y 27–30 | Preparar datos, invocar la unidad y realizar aserciones. Datos y dobles nuevos para cada prueba mediante `@BeforeEach`, equivalente en propósito al setup descrito. |
| Mismo PDF | p. 92, solución de calculadora | Casos ordinarios, límites, excepciones y propósito identificable. No se transfiere el objetivo de cobertura del ejercicio al proyecto ni se informa un porcentaje. |
| `Clases/Semana 11 - ISW2 2026-2 Pruebas Integrales - v2.pdf` | pp. 13–14, dobles y mocks | Controlar respuestas y observar interacciones y argumentos. |
| Mismo PDF | pp. 15–18 | Mockito con JUnit; `when(...).thenReturn(...)`, `verify(...)` y simulación de excepciones. La ausencia de cobro/registro/correo se comprueba con contadores de los dobles y `assertEquals(0, ...)`, sin agregar una operación de Mockito no mostrada en clase. |
| Mismo PDF | pp. 19–21 | Spy que ejecuta el doble local y permite verificar la llamada; diferencia con integración real. |
| `Clases/Labs/2026-2.TallerMocks-main/2026-2.TallerMocks-main/README.md` | «Cómo Ejecutar las Pruebas», «Mocks» | JUnit 5 y Mockito, pruebas en `src/test/java`, ejecución con Maven. La sección de PIT se revisó, pero no se aplica mutación en esta tarea. |
| Mismo laboratorio, `src/main/java/pe/edu/ulima/is2/service/SalesService.java` | Constructor y `processSale` | Recibir el repositorio por constructor, conservar real el servicio, sustituir el colaborador. |
| Mismo laboratorio, `src/test/java/pe/edu/ulima/is2/service/TestSalesService.java` | `setUp`, `testC1`, `testC2`, `testC3`, `testSetStock` | `mock`, respuestas y errores preparados, `assertThrows`, resultado esperado y comprobación del efecto. Se usan nombres `test...` para casos nuevos y se conservan los nombres identificables de los existentes. |
| Mismo laboratorio, `docs/reqs.md` y `docs/especificacion.md` | RF-02 y §2, `processSale` | Separar entradas, condiciones, resultado y errores. Las reglas de videojuegos no se convierten en reglas de BusTracka. |
| `Clases/Labs/PruebasUnitarias/PruebasUnitarias/src/test/java/pe/edu/ulima/is2/calculadora/TestCalculadora.java` | `testSumarCorrecto`, `testDividirCorrecto`, `testDividirEntreCeroError` | Datos de entrada explícitos, resultado esperado, `assertEquals` y aserción del error. |
| `Clases/Semana 9 - ISW2 2026-2 Aseguramiento de la Calidad del Software - v2.pdf` | Clasificación de pruebas, pp. 53 y 70–74, revisadas en el trabajo previo de esta carpeta | Contexto de nivel/enfoque/técnica; no se deriva de allí un patrón nuevo para implementar estas pruebas. |

También se leyeron los `pom.xml`, la especificación y los fuentes de los laboratorios. Se mantiene el JUnit/Mockito que ya proporciona `spring-boot-starter-test` en el proyecto; no se copiaron las versiones del laboratorio ni se añadió ninguna dependencia.

El laboratorio utiliza `@BeforeAll` para un mock compartido; la teoría describe setup por caso y la clase local ya utiliza `@BeforeEach`. Se mantiene esa organización para que cada caso tenga datos independientes. La referencia histórica a `TestCase`/JUnit 4 en T10 no se mezcla con JUnit Jupiter usado por los fuentes del laboratorio y el proyecto.

## Ajuste necesario para aislar los DAO

Antes del cambio, los cuatro DAO se inicializaban dentro del servicio con métodos estáticos de `DaoFactory`. Las pruebas existentes interceptaban esa fábrica con `MockedStatic` y usaban `ArgumentCaptor` y `thenAnswer`. No se encontró enseñanza de esos recursos específicos en los materiales revisados; conservarlos por estar en el código habría incumplido la restricción metodológica.

Se explicó esta limitación antes de editar producción. El único cambio de producción está en `VentaServiceImpl.java`: los DAO siguen siendo campos `final`, pero pueden recibirse por un constructor de acceso de paquete, del mismo modo que `SalesService` recibe su repositorio en el laboratorio. El constructor público conserva su firma y sigue obteniendo los mismos DAO de `DaoFactory`; delega en el constructor adicional. `@Autowired` identifica explícitamente el constructor público para el contenedor existente al haber dos constructores. Es configuración de la clase existente, no una herramienta ni un patrón de prueba añadido.

No se cambió el cuerpo de `comprar()`, las validaciones, el cobro, las excepciones, la generación de QR, el correo ni los demás métodos. No se hicieron públicos métodos privados. La compilación fue satisfactoria. La selección del constructor público por Spring se comprobó además mediante un arranque real de la aplicación, separado de las 15 pruebas unitarias y descrito más adelante. En los casos unitarios no se levanta el contenedor.

Dobles empleados:

- Mocks ordinarios para `ViajeDao`, `AsientoDao` y `UsuarioDao`; el PDF se sustituye por un mock de `GeneradorComprobante`, aunque `comprar()` no lo utiliza.
- `PasarelaDePrueba` devuelve aprobación/rechazo configurados y cuenta las solicitudes de cobro; su spy permite verificar los argumentos de cobro y anulación sin pasarela real.
- `VentaDaoDePrueba` recibe la venta y copia sus detalles en memoria, devuelve un ID ficticio y conserva código/fecha generados por el servicio. Puede lanzar la excepción configurada. Se observa como spy; no intenta reproducir una base de datos.
- `CorreoDePrueba` conserva destinatario, asunto y cuerpos en memoria, con contador de envíos; se observa como spy y nunca envía correo real.
- `GeneradorQrDePrueba` devuelve el marcador `QR`; el spy permite verificar exactamente la URL recibida. No se decodifica ni se genera una imagen real.
- `EnlaceSeguimientoDePrueba` devuelve un enlace ficticio y no crea tokens ni registros externos.

Estos dobles aplican los conceptos de stub y spy de T10 p. 7 y T11 pp. 13 y 19. Se leen los datos del objeto spy efectivamente entregado al servicio. No se utiliza reflexión, captura de argumentos, mocks estáticos, extensiones nuevas, parametrización ni esperas.

## Datos comunes

Viaje ficticio `9`, bus `ABC-123`, Lima–Ica, precio unitario `BigDecimal("45.50")`, salida siete días después del momento de preparación. Para el caso pasado se usan siete días antes. El pasajero es `Ana Torres`, titular `ANA TORRES`, número ficticio `4111111111111111`. Comprador `1`, nombre `ana`, correo `ana@test.pe`. Venta registrada por el doble: ID `3`. Aprobación simulada: código de transacción `SIM-1`.

Escenario base de asientos: `11` libre y `12` ocupado. Los casos de varios/seis asientos sustituyen explícitamente ese catálogo por uno libre. Se utilizan listas distintas y fechas holgadas, sin intentar acertar un instante de frontera. Los códigos aleatorios y fechas generados por el servicio se conservan y comparan por relación con la respuesta; no se fijan a un literal.

## Los 15 casos de comprar()

La última columna se contrastó con los elementos testcase del XML Surefire de la suite completa. Los 15 nombres están presentes sin failure, error ni skipped. «Aprobado» expresa únicamente que las aserciones de ese caso ejecutado se cumplieron. Los números siguen el orden pedido por el usuario.

| Nº | Nombre de prueba | Preparación | Resultado esperado | Procedencia | Resultado obtenido |
|---|---|---|---|---|---|
| 1 | `testCompraValidaDeUnAsiento` | Asiento 11 libre; aprobación de S/ 45.50 | Total de respuesta y venta S/ 45.50; detalle exacto de asiento, viaje, pasajero y precio; cobro correcto y últimos cuatro dígitos | Nuevo; reutiliza la comprobación de importe/últimos dígitos del caso compuesto existente | Aprobado: ejecutado, sin fallos ni errores |
| 2 | `testCompraValidaDeVariosAsientos` | Asientos 11 y 13 libres; aprobación de S/ 91.00 | Total exacto, dos detalles correctos y un solo registro; cobro de S/ 91.00 | Nuevo | Aprobado: ejecutado, sin fallos ni errores |
| 3 | `testCompraValidaDeSeisAsientos` | IDs distintos 1–6 libres; aprobación de S/ 273.00 | Aceptación del límite, total exacto, seis IDs registrados y seis asientos en comprobante; un registro | Nuevo | Aprobado: ejecutado, sin fallos ni errores |
| 4 | `noMasDeSeisAsientosPorCompra` | Siete IDs distintos 1–7 | `ReglaNegocioException`, mensaje del máximo; cero llamadas de cobro y cero registros | Existente ampliado | Aprobado: ejecutado, sin fallos ni errores |
| 5 | `testViajeInexistente` | `buscarPorId(9)` devuelve vacío | `RecursoNoEncontradoException`; sin cobro ni registro | Nuevo | Aprobado: ejecutado, sin fallos ni errores |
| 6 | `testViajeQueYaSalio` | Viaje con salida siete días antes | `ReglaNegocioException`; sin cobro ni registro | Nuevo | Aprobado: ejecutado, sin fallos ni errores |
| 7 | `testAsientoAjenoAlViaje` | Solicitud de ID 99 ausente del catálogo | `ReglaNegocioException`; sin cobro ni registro | Nuevo | Aprobado: ejecutado, sin fallos ni errores |
| 8 | `noVendeUnAsientoOcupado` | Solo ID 12 ocupado | `AsientoNoDisponibleException`; sin cobro ni registro | Existente ampliado | Aprobado: ejecutado, sin fallos ni errores |
| 9 | `testAsientoLibreSeguidoDeOcupado` | Lista [11,12], libre antes que ocupado | Rechazo íntegro con `AsientoNoDisponibleException`; sin cobro ni registro parcial | Nuevo | Aprobado: ejecutado, sin fallos ni errores |
| 10 | `pagoRechazadoNoRegistraLaVenta` | Pasarela responde rechazo al cobro exacto de S/ 45.50 | `PagoRechazadoException` y mensaje; se solicitó ese cobro, cero registros | Existente ampliado | Aprobado: ejecutado, sin fallos ni errores |
| 11 | `siOtroCompraElAsientoDuranteElPagoSeAnulaElCobro` | Pago aprobado `SIM-1`; doble de DAO lanza `AsientoNoDisponibleException` al registrar | Un intento de registro, solicitud de anulación de `SIM-1` y propagación de la misma excepción | Existente ampliado | Aprobado: ejecutado, sin fallos ni errores |
| 12 | `testErrorDePersistenciaAnulaElCobro` | Pago aprobado; doble de DAO lanza `PersistenciaException` con causa ficticia | Un intento de registro, solicitud de anulación de `SIM-1` y propagación del mismo error | Nuevo | Aprobado: ejecutado, sin fallos ni errores |
| 13 | `testQrDelBoletoEnElComprobante` | Compra exitosa y generador stub/spy | Solicitar exactamente `http://localhost:5173/boleto/{codigoComprobante}` y devolver `QR` en el comprobante; conservar el código registrado | Nuevo, separa el contrato del QR del caso compuesto de correo | Aprobado: ejecutado, sin fallos ni errores |
| 14 | `compraGuardaSoloLosUltimosDigitosYEnviaElCorreoConQrYEnlace` | Compra exitosa, comprador conocido y enlace ficticio | Un correo a `ana@test.pe`; asunto/cuerpo con código, total S/ 45.50, enlace al comprobante y al seguimiento; HTML con QR marcador y datos; últimos cuatro dígitos | Existente ampliado | Aprobado: ejecutado, sin fallos ni errores |
| 15 | `testCompradorNoEncontradoNoEnviaCorreo` | Pago/registro exitosos; comprador devuelve vacío | Devolver venta 3 PAGADA, total S/ 45.50, un registro y ningún envío de correo | Nuevo; comportamiento actual, no requisito aprobado | Aprobado: ejecutado, sin fallos ni errores |

Resumen de procedencia: cinco casos existentes sobre `comprar()` conservan sus propósitos y se amplían/adaptan (4, 8, 10, 11, 14); diez son nuevos (1, 2, 3, 5, 6, 7, 9, 12, 13, 15). No se agregaron quince duplicados. El caso de otro método permanece separado, con su cuerpo original.

Los casos 11 y 12 tienen propósitos distintos: el 11 comprueba la reacción a una colisión de disponibilidad comunicada por persistencia; el 12 comprueba la compensación ante otro fallo de almacenamiento. Ambos simulan la excepción en el colaborador. No demuestran simultaneidad real, transacciones PostgreSQL, reversión bancaria ni persistencia efectiva.

## Comandos y resultados reales

Entorno: Java Temurin 17.0.15 y Maven 3.9.16, ya instalados. Maven no está en PATH; se utilizó su ruta en IntelliJ y se definió JAVA_HOME solo para los procesos lanzados. No se cambió pom.xml ni se añadieron dependencias. Con autorización del usuario, Maven descargó las dependencias declaradas por el proyecto y los componentes de sus plugins desde Central. No se consultó Internet para incorporar teoría.

Los intentos anteriores sin conexión no habían llegado a compilar porque faltaba el POM padre Spring Boot 3.3.4. Ese impedimento quedó resuelto mediante la descarga autorizada. Los resultados actuales sustituyen los estados pendientes del informe anterior.

Primero se ejecutó la clase específica, con conexión y sin modo offline:

```powershell
$env:JAVA_HOME='C:/Users/davo2/.jdks/temurin-17.0.15'
& 'C:/Program Files/JetBrains/IntelliJ IDEA 2026.2.3/plugins/maven-plugin/lib/maven3/bin/mvn.cmd' -f BusTracka/backend/pom.xml '-Dtest=VentaServiceImplTest' test
```

Resultado: código de salida 0, BUILD SUCCESS; 16 pruebas, 0 fallos, 0 errores, 0 omitidas. Incluye 15 de comprar() y una de validarBoleto(). Compiló 99 fuentes de producción y 10 fuentes de pruebas con release 17. Finalizó el 14/11/2026 a las 12:34:30 -05:00; duración Maven 35.014 s, incluida resolución de dependencias; tiempo de la clase 1.353 s.

Después se ejecutó la suite completa:

```powershell
$env:JAVA_HOME='C:/Users/davo2/.jdks/temurin-17.0.15'
& 'C:/Program Files/JetBrains/IntelliJ IDEA 2026.2.3/plugins/maven-plugin/lib/maven3/bin/mvn.cmd' -B -ntp -f BusTracka/backend/pom.xml test
```

Resultado: código de salida 0, BUILD SUCCESS; 54 pruebas, 0 fallos, 0 errores y 0 omitidas. Finalizó el 14/11/2026 a las 12:34:56 -05:00; duración Maven 5.776 s. La clase VentaServiceImplTest volvió a ejecutar sus 16 casos, sin fallos, en 0.237 s. No se suman ambas ejecuciones para afirmar que hay más casos distintos.

Reportes reales de la última ejecución: `BusTracka/backend/target/surefire-reports/TEST-*.xml` y sus correspondientes `.txt`. Para los casos de este entregable: `TEST-pe.edu.ulima.is2.bustracka.service.impl.VentaServiceImplTest.xml` y `pe.edu.ulima.is2.bustracka.service.impl.VentaServiceImplTest.txt` dentro de esa carpeta. Son archivos generados por Maven y pueden reemplazarse al volver a ejecutar.

| Clase | Ejecutadas | Fallos | Errores | Omitidas |
|---|---|---|---|---|
| `GeneradorComprobantePdfTest` | 1 | 0 | 0 | 0 |
| `EstadoPasajeTest` | 4 | 0 | 0 | 0 |
| `PasarelaPagoSimuladaTest` | 2 | 0 | 0 | 0 |
| `GeneradorQrZxingTest` | 1 | 0 | 0 | 0 |
| `JwtAuthFilterTest` | 5 | 0 | 0 | 0 |
| `AuthServiceImplTest` | 11 | 0 | 0 | 0 |
| `EnlaceSeguimientoServiceImplTest` | 8 | 0 | 0 | 0 |
| `VentaServiceImplTest` | 16 | 0 | 0 | 0 |
| `CalculadoraItinerarioTest` | 4 | 0 | 0 | 0 |
| `SimuladorUbicacionTest` | 2 | 0 | 0 | 0 |

No aparecieron errores de compilación ni fallos de pruebas que requirieran corrección. No se modificaron las aserciones, reglas de negocio ni fuentes de pruebas durante esta verificación con conexión. La advertencia de OpenJDK sobre class data sharing durante el uso de Mockito no impidió la ejecución ni fue un error Surefire.

## Revisión del constructor y arranque Spring

Se contrastó nuevamente el constructor de `SalesService` del laboratorio con el constructor de acceso de paquete de VentaServiceImpl: ambos reciben colaboradores por constructor. Los casos mantienen el servicio real y sustituyen únicamente esos colaboradores con los dobles y spies permitidos por T10 p. 7 y T11 pp. 13–19. No se añadieron marcos de pruebas de contexto, reflexión ni interceptación de estáticos.

El constructor público conserva la firma original de seis parámetros, el @Value de la URL y la creación de los mismos cuatro DAO mediante DaoFactory. @Autowired hace explícita su elección por Spring al existir ahora otra sobrecarga; el constructor adicional no obliga a registrar beans DAO. No hay cambios en comprar() ni métodos privados expuestos. La anotación pertenece al framework de producción existente; no es una técnica nueva de pruebas.

Se comprobó el arranque real mediante el plugin Spring Boot ya declarado en el proyecto y utilizado para ejecutarlo, separado de la metodología de las 15 pruebas unitarias:

```powershell
$env:JAVA_HOME='C:/Users/davo2/.jdks/temurin-17.0.15'
& 'C:/Program Files/JetBrains/IntelliJ IDEA 2026.2.3/plugins/maven-plugin/lib/maven3/bin/mvn.cmd' -B -ntp -f BusTracka/backend/pom.xml spring-boot:run '-Dspring-boot.run.arguments=--server.address=127.0.0.1 --server.port=0 --notificacion.webhook-url='
```

La primera invocación en terminal interactiva fue rechazada por el entorno antes de crear el proceso. Se ejecutó el mismo comando sin terminal interactiva, con salida capturada por un proceso auxiliar local, sin instalar herramientas. El auxiliar comprobó la línea de arranque y terminó exclusivamente el árbol de procesos que había creado para esta comprobación.

Resultado observado: contexto web inicializado, Tomcat iniciado en el puerto local asignado 51547 y `Started BusTrackaApplication in 1.472 seconds` a las 12:36:02.727 -05:00. Esto confirma que la selección e inyección del constructor permiten arrancar la aplicación. Log real: `BusTracka/backend/target/arranque-spring.log`. No se presenta el cierre controlado del proceso como un BUILD SUCCESS de una aplicación que normalmente permanece ejecutándose.

Se limitó el servidor a 127.0.0.1 y se dejó que el sistema asignara un puerto para evitar colisiones; el webhook de correo se desactivó solo por argumento de ejecución. No se enviaron solicitudes de negocio, correos ni compras ni se levantaron servicios externos. No se cambió application.properties. La advertencia de clave JWT aleatoria corresponde a la configuración de desarrollo sin JWT_SECRET; no impidió el arranque.

Esta comprobación valida creación de beans y arranque, no una compra real contra PostgreSQL ni la disponibilidad operativa de todos los endpoints. No se añade como caso número 16 de comprar() ni se mezcla con el conteo JUnit.

## Limitaciones y seguimiento

- Los 15 casos de comprar(), el de validarBoleto() y los demás casos de la suite sí se ejecutaron. No quedaron casos de esta tabla pendientes ni omitidos. No se midió cobertura de código y no se atribuye un porcentaje a estos resultados.
- Los resultados unitarios dependen de dobles controlados. No demuestran concurrencia real, transacciones de PostgreSQL, reversión bancaria ni entrega real de correo. La conectividad a Internet fue necesaria para resolver dependencias Maven; no convierte a las pruebas de comprar() en pruebas de servicios externos.
- `testCompradorNoEncontradoNoEnviaCorreo` documenta comportamiento actual y no lo presenta como política de negocio aprobada ni demuestra que la base real permita ese estado.
- La suite completa incluye sus propios casos de generador PDF, QR real y pasarela simulada; sus resultados están en sus clases respectivas. Los 15 casos de comprar() siguen aislando esos colaboradores y no prueban sus implementaciones reales.
- No se detectaron defectos de negocio ni errores de compilación/pruebas en estas ejecuciones. El cambio de producción sigue limitado al constructor explicado y su selección explícita por Spring; no fue necesario añadir otra corrección.
- Se conservaron rama y commit de referencia, sin actualizar el repositorio. No se hicieron commits, pushes ni merges. Los reportes de target son artefactos locales de ejecución.
