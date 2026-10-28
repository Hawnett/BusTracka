package pe.edu.ulima.is2.bustracka.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.ulima.is2.bustracka.dao.AsientoDao;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.UsuarioDao;
import pe.edu.ulima.is2.bustracka.dao.VentaDao;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.dto.AsientoVendidoDTO;
import pe.edu.ulima.is2.bustracka.dto.CompraRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.ComprobanteResponseDTO;
import pe.edu.ulima.is2.bustracka.exception.AsientoNoDisponibleException;
import pe.edu.ulima.is2.bustracka.exception.PagoRechazadoException;
import pe.edu.ulima.is2.bustracka.exception.RecursoNoEncontradoException;
import pe.edu.ulima.is2.bustracka.exception.ReglaNegocioException;
import pe.edu.ulima.is2.bustracka.model.Asiento;
import pe.edu.ulima.is2.bustracka.model.DetalleVenta;
import pe.edu.ulima.is2.bustracka.model.Usuario;
import pe.edu.ulima.is2.bustracka.model.Venta;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.notificacion.NotificacionService;
import pe.edu.ulima.is2.bustracka.pago.PasarelaPago;
import pe.edu.ulima.is2.bustracka.pago.ResultadoPago;
import pe.edu.ulima.is2.bustracka.pago.SolicitudPago;
import pe.edu.ulima.is2.bustracka.qr.GeneradorQr;
import pe.edu.ulima.is2.bustracka.security.JwtUtil;
import pe.edu.ulima.is2.bustracka.service.VentaService;
import pe.edu.ulima.is2.bustracka.util.ZonaApp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orquesta la compra. Las dependencias externas (pasarela, correo, QR) entran
 * por interfaz (DIP), asi el service se puede probar con dobles y cambiar la
 * pasarela real no lo afecta.
 */
@Service
public class VentaServiceImpl implements VentaService {

    private static final int MAX_ASIENTOS_POR_COMPRA = 6;
    private static final long VIGENCIA_ENLACE_MS = 7L * 24 * 60 * 60 * 1000;

    private final VentaDao ventaDao = DaoFactory.crearVentaDao();
    private final ViajeDao viajeDao = DaoFactory.crearViajeDao();
    private final AsientoDao asientoDao = DaoFactory.crearAsientoDao();
    private final UsuarioDao usuarioDao = DaoFactory.crearUsuarioDao();

    private final PasarelaPago pasarelaPago;
    private final NotificacionService notificacionService;
    private final GeneradorQr generadorQr;
    private final JwtUtil jwtUtil;
    private final String urlFrontend;

    public VentaServiceImpl(PasarelaPago pasarelaPago, NotificacionService notificacionService,
                            GeneradorQr generadorQr, JwtUtil jwtUtil,
                            @Value("${app.frontend-url:http://localhost:5173}") String urlFrontend) {
        this.pasarelaPago = pasarelaPago;
        this.notificacionService = notificacionService;
        this.generadorQr = generadorQr;
        this.jwtUtil = jwtUtil;
        this.urlFrontend = urlFrontend;
    }

    @Override
    public ComprobanteResponseDTO comprar(Long idUsuario, CompraRequestDTO request) {
        Viaje viaje = viajeDao.buscarPorId(request.idViaje())
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje no existe"));
        if (!viaje.fechaSalida().isAfter(ZonaApp.ahora())) {
            throw new ReglaNegocioException("Este viaje ya salio, no se pueden comprar pasajes");
        }

        List<DetalleVenta> detalles = validarAsientos(viaje, request);
        BigDecimal total = viaje.precio().multiply(BigDecimal.valueOf(detalles.size()));

        ResultadoPago pago = pasarelaPago.cobrar(
                new SolicitudPago(total, request.numeroTarjeta(), request.titularTarjeta()));
        if (!pago.aprobado()) {
            throw new PagoRechazadoException(pago.mensaje());
        }

        // Si otro usuario gano el asiento (o falla la BD) despues de cobrar, se reversa el cobro.
        Venta venta;
        try {
            venta = ventaDao.registrar(new Venta(null, idUsuario, total, "PAGADA", pago.codigoTransaccion(),
                    "BT-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase(), ZonaApp.ahora()), detalles);
        } catch (RuntimeException e) {
            pasarelaPago.anular(pago.codigoTransaccion());
            throw e;
        }

        ComprobanteResponseDTO comprobante = construirComprobante(venta, detalles, viaje);
        enviarCorreo(idUsuario, comprobante);
        return comprobante;
    }

    @Override
    public ComprobanteResponseDTO obtenerComprobante(Long idUsuario, Long idVenta) {
        Venta venta = ventaDao.buscarPorId(idVenta)
                .filter(v -> v.idUsuario().equals(idUsuario)) // no revelar compras ajenas
                .orElseThrow(() -> new RecursoNoEncontradoException("La compra no existe"));
        return reconstruir(venta);
    }

    @Override
    public List<ComprobanteResponseDTO> listarCompras(Long idUsuario) {
        return ventaDao.listarPorUsuario(idUsuario).stream().map(this::reconstruir).toList();
    }

    private ComprobanteResponseDTO reconstruir(Venta venta) {
        List<DetalleVenta> detalles = ventaDao.listarDetalles(venta.idVenta());
        Viaje viaje = viajeDao.buscarPorId(detalles.get(0).idViaje())
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje de la compra ya no existe"));
        return construirComprobante(venta, detalles, viaje);
    }

    private List<DetalleVenta> validarAsientos(Viaje viaje, CompraRequestDTO request) {
        Set<Long> ids = new LinkedHashSet<>(request.idAsientos());
        if (ids.size() > MAX_ASIENTOS_POR_COMPRA) {
            throw new ReglaNegocioException("Maximo " + MAX_ASIENTOS_POR_COMPRA + " asientos por compra");
        }
        Map<Long, Asiento> asientosDelViaje = asientoDao.listarPorViaje(viaje.idViaje()).stream()
                .collect(Collectors.toMap(Asiento::idAsiento, Function.identity()));

        List<DetalleVenta> detalles = new ArrayList<>();
        for (Long id : ids) {
            Asiento asiento = asientosDelViaje.get(id);
            if (asiento == null) {
                throw new ReglaNegocioException("El asiento " + id + " no pertenece a este viaje");
            }
            if (asiento.ocupado()) {
                throw new AsientoNoDisponibleException("El asiento " + asiento.numero() + " ya fue vendido");
            }
            detalles.add(new DetalleVenta(viaje.idViaje(), id, asiento.numero(),
                    request.nombrePasajero(), viaje.precio()));
        }
        return detalles;
    }

    private ComprobanteResponseDTO construirComprobante(Venta venta, List<DetalleVenta> detalles, Viaje viaje) {
        String token = jwtUtil.generarTokenSeguimiento(viaje.idViaje(), VIGENCIA_ENLACE_MS);
        String enlace = urlFrontend + "/seguimiento/" + token;
        String qr = generadorQr.generarPngBase64("BUSTRACKA:" + venta.codigoComprobante());
        List<AsientoVendidoDTO> asientos = detalles.stream()
                .map(d -> new AsientoVendidoDTO(d.numeroAsiento(), d.nombrePasajero(), d.precio()))
                .toList();
        return new ComprobanteResponseDTO(venta.idVenta(), venta.codigoComprobante(), venta.estado(),
                venta.total(), venta.fecha(), viaje.idViaje(), viaje.ciudadOrigen(), viaje.ciudadDestino(),
                viaje.fechaSalida(), viaje.placa(), asientos, venta.codigoPago(), qr, enlace);
    }

    private void enviarCorreo(Long idUsuario, ComprobanteResponseDTO c) {
        Usuario usuario = usuarioDao.buscarPorId(idUsuario).orElse(null);
        if (usuario == null) {
            return;
        }
        String cuerpo = "Hola " + usuario.getNombreUsuario() + ", tu compra " + c.codigoComprobante()
                + " (" + c.ciudadOrigen() + " -> " + c.ciudadDestino() + ", " + c.fechaSalida()
                + ") fue confirmada por S/ " + c.total() + ".\nSigue tu bus en tiempo real: " + c.enlaceSeguimiento();
        notificacionService.enviarComprobante(usuario.getEmail(), "Tu comprobante BusTracka " + c.codigoComprobante(), cuerpo);
    }
}
