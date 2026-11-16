# Caja blanca: `VentaDaoImpl.registrar(Venta, List<DetalleVenta>)`

Cosette Mañuico Jáuregui

**Método registrar():** guarda una venta y sus asientos dentro de una transacción (si algo falla, se deshace todo
con `rollback`). Concentra la regla más importante de la base de datos: la restricción
`UNIQUE (id_viaje, id_asiento)` de `detalle_venta`, que impide vender dos veces el mismo asiento.

## 1. Nodos del grafo de flujo

| Nodo | Instrucción |
|---|---|
| 1 | Obtener la conexión del pool y desactivar autoCommit. ¿Falla la conexión? (predicado) |
| 2 | `INSERT INTO venta` y leer el id generado |
| 3 | `for (DetalleVenta d : detalles)`: ¿quedan detalles? (predicado) |
| 4 | `INSERT INTO detalle_venta` |
| 5 | `commit()` |
| 6 | ¿Se produjo `SQLException` en la transacción? (predicado) |
| 7 | `return new Venta(idVenta, ...)` |
| 8 | `rollback()`. ¿El SQLState es 23505 (violación de UNIQUE)? (predicado) |
| 9 | `throw AsientoNoDisponibleException` |
| 10 | `throw e` |
| 11 | catch externo: `throw PersistenciaException` |
| 12 | Fin |

Aristas: 1-2, 1-11, 2-3, 3-4, 4-3, 3-5, 5-6, 6-7, 6-8, 8-9, 8-10, 10-11, 7-12, 9-12, 11-12 (15 aristas).

## 2. Complejidad ciclomática

V(G) = aristas − nodos + 2 = 15 − 12 + 2 = **5**. Comprobación: nodos predicado + 1 = 4 + 1 = 5.
Como V(G) = 5 > 4, hacen falta 5 caminos independientes.

## 3. Caminos y casos de prueba

| Camino | Recorrido | Caso | Resultado esperado |
|---|---|---|---|
| C1 | 1-11-12 | Base de datos apagada | `PersistenciaException` |
| C2 | 1-2-3-5-6-7-12 | Venta sin detalles | Venta guardada sin asientos |
| C3 | 1-2-3-4-3-5-6-7-12 | Venta con 1 asiento libre | Venta guardada con su id |
| C4 | 1-2-3-4-3-5-6-8-9-12 | Asiento ya vendido en el mismo viaje | `AsientoNoDisponibleException` y rollback |
| C5 | 1-2-3-4-3-5-6-8-10-11-12 | `id_asiento` que no existe (viola la FK) | Rollback y `PersistenciaException` |

El grafo editable está en el informe final (`drawio/grafo_caja_blanca_registrar.drawio`).