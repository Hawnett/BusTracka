package pe.edu.ulima.is2.bustracka.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.EnlaceSeguimientoDao;
import pe.edu.ulima.is2.bustracka.dao.VentaDao;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.dto.EnlaceSeguimientoDTO;
import pe.edu.ulima.is2.bustracka.dto.SeguimientoCompartidoDTO;
import pe.edu.ulima.is2.bustracka.exception.EnlaceInvalidoException;
import pe.edu.ulima.is2.bustracka.exception.RecursoNoEncontradoException;
import pe.edu.ulima.is2.bustracka.exception.ReglaNegocioException;
import pe.edu.ulima.is2.bustracka.model.DetalleVenta;
import pe.edu.ulima.is2.bustracka.model.EnlaceSeguimiento;
import pe.edu.ulima.is2.bustracka.model.EstadoViaje;
import pe.edu.ulima.is2.bustracka.model.Venta;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.qr.GeneradorQr;
import pe.edu.ulima.is2.bustracka.service.EnlaceSeguimientoService;
import pe.edu.ulima.is2.bustracka.service.SeguimientoService;
import pe.edu.ulima.is2.bustracka.util.ZonaApp;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * El token del enlace es aleatorio y se guarda en la tabla seguimiento con su
 * vencimiento: el final del dia en que llega el viaje.
 */
@Service
public class EnlaceSeguimientoServiceImpl implements EnlaceSeguimientoService {

    private static final String ENLACE_INVALIDO = "El enlace de seguimiento no es valido o ya expiro";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final EnlaceSeguimientoDao enlaceDao = DaoFactory.crearEnlaceSeguimientoDao();
    private final VentaDao ventaDao = DaoFactory.crearVentaDao();
    private final ViajeDao viajeDao = DaoFactory.crearViajeDao();

    private final SeguimientoService seguimientoService;
    private final GeneradorQr generadorQr;
    private final String urlFrontend;

    public EnlaceSeguimientoServiceImpl(SeguimientoService seguimientoService, GeneradorQr generadorQr,
                                        @Value("${app.frontend-url:http://localhost:5173}") String urlFrontend) {
        this.seguimientoService = seguimientoService;
        this.generadorQr = generadorQr;
        this.urlFrontend = urlFrontend;
    }

    @Override
    public EnlaceSeguimientoDTO obtenerOCrear(Venta venta, Viaje viaje) {
        EnlaceSeguimiento enlace = enlaceDao.buscarActivoPorVenta(venta.idVenta())
                .orElseGet(() -> enlaceDao.registrar(new EnlaceSeguimiento(null, venta.idVenta(), viaje.idViaje(),
                        nuevoToken(), ZonaApp.ahora(), finDelViaje(viaje), EnlaceSeguimiento.ACTIVO)));
        return new EnlaceSeguimientoDTO(venta.idVenta(), url(enlace.token()), vigencia(enlace, viaje), null);
    }

    @Override
    public Optional<String> urlVigente(Venta venta, Viaje viaje) {
        if (!venta.estaPagada() || viaje.estadoEn(ZonaApp.ahora()) == EstadoViaje.FINALIZADO) {
            return Optional.empty();
        }
        return enlaceDao.buscarActivoPorVenta(venta.idVenta()).map(e -> url(e.token()));
    }

    @Override
    public EnlaceSeguimientoDTO generarParaCompra(Long idUsuario, Long idVenta) {
        Venta venta = ventaDao.buscarPorId(idVenta)
                .filter(v -> v.idUsuario().equals(idUsuario))
                .orElseThrow(() -> new RecursoNoEncontradoException("La compra no existe"));
        if (!venta.estaPagada()) {
            throw new ReglaNegocioException("La compra fue anulada");
        }
        Viaje viaje = viajeDeLaVenta(venta);
        if (viaje.estadoEn(ZonaApp.ahora()) == EstadoViaje.FINALIZADO) {
            throw new ReglaNegocioException("El viaje ya termino, solo se puede compartir un viaje activo");
        }
        EnlaceSeguimientoDTO enlace = obtenerOCrear(venta, viaje);
        return new EnlaceSeguimientoDTO(enlace.idVenta(), enlace.url(), enlace.expiraEn(),
                generadorQr.generarPngBase64(enlace.url()));
    }

    @Override
    public SeguimientoCompartidoDTO verCompartido(String token) {
        EnlaceSeguimiento enlace = enlaceDao.buscarPorToken(token)
                .filter(EnlaceSeguimiento::estaActivo)
                .orElseThrow(() -> new EnlaceInvalidoException(ENLACE_INVALIDO));
        Venta venta = ventaDao.buscarPorId(enlace.idVenta())
                .filter(Venta::estaPagada)
                .orElseThrow(() -> new EnlaceInvalidoException(ENLACE_INVALIDO));
        Viaje viaje = viajeDao.buscarPorId(enlace.idViaje())
                .orElseThrow(() -> new EnlaceInvalidoException(ENLACE_INVALIDO));
        LocalDateTime expira = vigencia(enlace, viaje);
        if (ZonaApp.ahora().isAfter(expira)) {
            throw new EnlaceInvalidoException(ENLACE_INVALIDO);
        }
        List<DetalleVenta> detalles = ventaDao.listarDetalles(venta.idVenta());
        return new SeguimientoCompartidoDTO(detalles.get(0).nombrePasajero(), venta.codigoComprobante(),
                detalles.stream().map(DetalleVenta::numeroAsiento).toList(), expira,
                seguimientoService.obtenerUbicacion(viaje.idViaje()));
    }

    private Viaje viajeDeLaVenta(Venta venta) {
        List<DetalleVenta> detalles = ventaDao.listarDetalles(venta.idVenta());
        return viajeDao.buscarPorId(detalles.get(0).idViaje())
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje de la compra ya no existe"));
    }

    /** Si despues se registra un retraso, el enlace sigue valido hasta el nuevo fin del viaje. */
    private static LocalDateTime vigencia(EnlaceSeguimiento enlace, Viaje viaje) {
        LocalDateTime fin = finDelViaje(viaje);
        return fin.isAfter(enlace.fechaExpiracion()) ? fin : enlace.fechaExpiracion();
    }

    private static LocalDateTime finDelViaje(Viaje viaje) {
        return viaje.llegadaEstimada().toLocalDate().atTime(23, 59, 59);
    }

    private static String nuevoToken() {
        byte[] bytes = new byte[24];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String url(String token) {
        return urlFrontend + "/seguimiento/" + token;
    }
}
