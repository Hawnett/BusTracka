package pe.edu.ulima.is2.bustracka.dao;

import pe.edu.ulima.is2.bustracka.model.DetalleVenta;
import pe.edu.ulima.is2.bustracka.model.Venta;

import java.util.List;
import java.util.Optional;

public interface VentaDao {
    /**
     * Guarda la venta y todos sus asientos en UNA transaccion. Lanza
     * AsientoNoDisponibleException si algun asiento ya fue vendido.
     */
    Venta registrar(Venta venta, List<DetalleVenta> detalles);

    Optional<Venta> buscarPorId(Long idVenta);

    List<Venta> listarPorUsuario(Long idUsuario);

    List<DetalleVenta> listarDetalles(Long idVenta);
}
