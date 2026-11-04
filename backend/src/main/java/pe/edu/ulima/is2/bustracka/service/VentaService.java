package pe.edu.ulima.is2.bustracka.service;

import pe.edu.ulima.is2.bustracka.dto.BoletoPublicoDTO;
import pe.edu.ulima.is2.bustracka.dto.CompraRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.ComprobanteResponseDTO;

import java.util.List;

public interface VentaService {
    ComprobanteResponseDTO comprar(Long idUsuario, CompraRequestDTO request);

    ComprobanteResponseDTO obtenerComprobante(Long idUsuario, Long idVenta);

    List<ComprobanteResponseDTO> listarCompras(Long idUsuario);

    byte[] generarPdf(Long idUsuario, Long idVenta);

    /** Pagina publica a la que lleva el QR del boleto. */
    BoletoPublicoDTO validarBoleto(String codigoComprobante);
}
