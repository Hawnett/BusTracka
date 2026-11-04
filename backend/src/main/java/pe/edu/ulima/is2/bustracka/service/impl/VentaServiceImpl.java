package pe.edu.ulima.is2.bustracka.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.ulima.is2.bustracka.comprobante.GeneradorComprobante;
import pe.edu.ulima.is2.bustracka.dao.AsientoDao;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.UsuarioDao;
import pe.edu.ulima.is2.bustracka.dao.VentaDao;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.dto.AsientoVendidoDTO;
import pe.edu.ulima.is2.bustracka.dto.BoletoPublicoDTO;
import pe.edu.ulima.is2.bustracka.dto.CompraRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.ComprobanteResponseDTO;
import pe.edu.ulima.is2.bustracka.exception.AsientoNoDisponibleException;
import pe.edu.ulima.is2.bustracka.exception.PagoRechazadoException;
import pe.edu.ulima.is2.bustracka.exception.RecursoNoEncontradoException;
import pe.edu.ulima.is2.bustracka.exception.ReglaNegocioException;
import pe.edu.ulima.is2.bustracka.model.Asiento;
import pe.edu.ulima.is2.bustracka.model.DetalleVenta;
import pe.edu.ulima.is2.bustracka.model.EstadoPasaje;
import pe.edu.ulima.is2.bustracka.model.EstadoViaje;
import pe.edu.ulima.is2.bustracka.model.Usuario;
import pe.edu.ulima.is2.bustracka.model.Venta;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.notificacion.NotificacionService;
import pe.edu.ulima.is2.bustracka.notificacion.PlantillaCorreo;
import pe.edu.ulima.is2.bustracka.pago.PasarelaPago;
import pe.edu.ulima.is2.bustracka.pago.ResultadoPago;
import pe.edu.ulima.is2.bustracka.pago.SolicitudPago;
import pe.edu.ulima.is2.bustracka.qr.GeneradorQr;
import pe.edu.ulima.is2.bustracka.service.EnlaceSeguimientoService;
import pe.edu.ulima.is2.bustracka.service.VentaService;
import pe.edu.ulima.is2.bustracka.util.ZonaApp;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orquesta la compra. Las dependencias externas (pasarela, correo, QR, PDF)
 * entran por interfaz (DIP), asi el service se puede probar con dobles y
 * cambiar la pasarela real no lo afecta.
 */
@Service
public class VentaServiceImpl implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);
    private static final int MAX_ASIENTOS_POR_COMPRA = 6;
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VentaDao ventaDao = DaoFactory.crearVentaDao();
    private final ViajeDao viajeDao = DaoFactory.crearViajeDao();
    private final AsientoDao asientoDao = DaoFactory.crearAsientoDao();
    private final UsuarioDao usuarioDao = DaoFactory.crearUsuarioDao();

    private final PasarelaPago pasarelaPago;
    private final NotificacionService notificacionService;
    private final GeneradorQr generadorQr;
    private final GeneradorComprobante generadorComprobante;
    private final EnlaceSeguimientoService enlaceSeguimientoService;
    private final String urlFrontend;

    public VentaServiceImpl(PasarelaPago pasarelaPago, NotificacionService notificacionService,
                            GeneradorQr generadorQr, GeneradorComprobante generadorComprobante,
                            EnlaceSeguimientoService enlaceSeguimientoService,
                            @Value("${app.frontend-url:http://localhost:5173}") String urlFrontend) {
        this.pasarelaPago = pasarelaPago;
        this.notificacionService = notificacionService;
        this.generadorQr = generadorQr;
        this.generadorComprobante = generadorComprobante;
        this.enlaceSeguimientoService = enlaceSeguimientoService;
        this.urlFrontend = urlFrontend;
    }

    @Override
    public ComprobanteResponseDTO comprar(Long idUsuario, CompraRequestDTO request) {
        Viaje viaje = viajeDao.buscarPorId(request.idViaje())
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje no existe"));
        if (!viaje.salidaEstimada().isAfter(ZonaApp.ahora())) {
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
        String tarjeta = request.numeroTarjeta();
        Venta venta;
        try {
            venta = ventaDao.registrar(new Venta(null, idUsuario, total, Venta.PAGADA, pago.codigoTransaccion(),
                    "BT-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase(),
                    tarjeta.substring(tarjeta.length() - 4), ZonaApp.ahora()), detalles);
        } catch (RuntimeException e) {
            pasarelaPago.anular(pago.codigoTransaccion());
            throw e;
        }

        // La venta ya quedo pagada: si el enlace falla, se vuelve a crear al abrir el comprobante.
        String enlace = null;
        try {
            enlace = enlaceSeguimientoService.obtenerOCrear(venta, viaje).url();
        } catch (RuntimeException e) {
            log.warn("No se pudo crear el enlace de seguimiento de la venta {}", venta.codigoComprobante(), e);
        }

        Usuario comprador = usuarioDao.buscarPorId(idUsuario).orElse(null);
        ComprobanteResponseDTO comprobante = construirComprobante(venta, detalles, viaje, nombre(comprador), enlace, true);
        enviarCorreo(comprador, comprobante);
        return comprobante;
    }

    @Override
    public ComprobanteResponseDTO obtenerComprobante(Long idUsuario, Long idVenta) {
        Venta venta = ventaDao.buscarPorId(idVenta)
                .filter(v -> v.idUsuario().equals(idUsuario)) // no revelar compras ajenas
                .orElseThrow(() -> new RecursoNoEncontradoException("La compra no existe"));
        List<DetalleVenta> detalles = ventaDao.listarDetalles(venta.idVenta());
        Viaje viaje = viajeDe(detalles);
        String enlace = venta.estaPagada() && viaje.estadoEn(ZonaApp.ahora()) != EstadoViaje.FINALIZADO
                ? enlaceSeguimientoService.obtenerOCrear(venta, viaje).url()
                : null;
        return construirComprobante(venta, detalles, viaje, nombre(usuarioDao.buscarPorId(idUsuario).orElse(null)), enlace, true);
    }

    @Override
    public List<ComprobanteResponseDTO> listarCompras(Long idUsuario) {
        String comprador = nombre(usuarioDao.buscarPorId(idUsuario).orElse(null));
        return ventaDao.listarPorUsuario(idUsuario).stream().map(venta -> {
            List<DetalleVenta> detalles = ventaDao.listarDetalles(venta.idVenta());
            Viaje viaje = viajeDe(detalles);
            String enlace = enlaceSeguimientoService.urlVigente(venta, viaje).orElse(null);
            return construirComprobante(venta, detalles, viaje, comprador, enlace, false);
        }).toList();
    }

    @Override
    public byte[] generarPdf(Long idUsuario, Long idVenta) {
        return generadorComprobante.generarPdf(obtenerComprobante(idUsuario, idVenta));
    }

    @Override
    public BoletoPublicoDTO validarBoleto(String codigoComprobante) {
        Venta venta = ventaDao.buscarPorCodigo(codigoComprobante)
                .orElseThrow(() -> new RecursoNoEncontradoException("El boleto no existe"));
        List<DetalleVenta> detalles = ventaDao.listarDetalles(venta.idVenta());
        Viaje viaje = viajeDe(detalles);
        EstadoPasaje estado = EstadoPasaje.de(venta, viaje, ZonaApp.ahora());
        boolean valido = estado == EstadoPasaje.PROXIMO || estado == EstadoPasaje.EN_CURSO;
        return new BoletoPublicoDTO(venta.codigoComprobante(), valido, estado.name(), detalles.get(0).nombrePasajero(),
                viaje.ciudadOrigen(), viaje.ciudadDestino(), viaje.terminalOrigen(), viaje.salidaEstimada(),
                viaje.placa(), detalles.stream().map(DetalleVenta::numeroAsiento).toList());
    }

    private Viaje viajeDe(List<DetalleVenta> detalles) {
        return viajeDao.buscarPorId(detalles.get(0).idViaje())
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje de la compra ya no existe"));
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

    private ComprobanteResponseDTO construirComprobante(Venta venta, List<DetalleVenta> detalles, Viaje viaje,
                                                        String comprador, String enlace, boolean incluirQr) {
        // El QR lleva a la pagina publica que valida el boleto.
        String qr = incluirQr ? generadorQr.generarPngBase64(urlFrontend + "/boleto/" + venta.codigoComprobante()) : null;
        List<AsientoVendidoDTO> asientos = detalles.stream()
                .map(d -> new AsientoVendidoDTO(d.numeroAsiento(), d.nombrePasajero(), d.precio()))
                .toList();
        String metodoPago = venta.tarjetaUltimos4() == null ? "Tarjeta" : "Tarjeta terminada en " + venta.tarjetaUltimos4();
        return new ComprobanteResponseDTO(venta.idVenta(), venta.codigoComprobante(), venta.estado(),
                EstadoPasaje.de(venta, viaje, ZonaApp.ahora()).name(), venta.total(), venta.fecha(), viaje.idViaje(),
                viaje.ciudadOrigen(), viaje.ciudadDestino(), viaje.terminalOrigen(), viaje.terminalDestino(),
                viaje.fechaSalida(), viaje.minutosRetraso(), viaje.salidaEstimada(), viaje.llegadaEstimada(),
                viaje.placa(), asientos, venta.codigoPago(), metodoPago, comprador, qr, enlace);
    }

    private void enviarCorreo(Usuario usuario, ComprobanteResponseDTO c) {
        if (usuario == null) {
            return;
        }
        Map<String, String> datos = PlantillaCorreo.filas();
        datos.put("Ruta", c.ciudadOrigen() + " → " + c.ciudadDestino());
        datos.put("Salida", c.salidaEstimada().format(FECHA_HORA));
        datos.put("Terminal", c.terminalOrigen());
        datos.put("Bus", c.placa());
        datos.put("Asientos", c.asientos().stream().map(a -> String.valueOf(a.numero())).collect(Collectors.joining(", ")));
        datos.put("Código", c.codigoComprobante());
        datos.put("Total", "S/ " + c.total());

        PlantillaCorreo correo = new PlantillaCorreo(usuario.getNombreUsuario())
                .parrafo("Gracias por tu compra. Tu pasaje ya está confirmado. Aquí tienes los detalles de tu viaje:")
                .datos(datos)
                .imagenQr(c.qrBase64(), "Tu código de abordaje")
                .boton("Ver mi comprobante", urlFrontend + "/comprobante/" + c.idVenta(), true);
        if (c.enlaceSeguimiento() != null) {
            correo.boton("Seguir mi viaje en tiempo real", c.enlaceSeguimiento(), false);
        }
        notificacionService.enviarCorreo(usuario.getEmail(), "Tu comprobante BusTracka " + c.codigoComprobante(),
                correo.texto(), correo.html());
    }

    private static String nombre(Usuario usuario) {
        return usuario == null ? null : usuario.getNombreUsuario();
    }
}
