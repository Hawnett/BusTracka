package pe.edu.ulima.is2.bustracka.dao;

import pe.edu.ulima.is2.bustracka.model.EnlaceSeguimiento;

import java.util.Optional;

public interface EnlaceSeguimientoDao {

    EnlaceSeguimiento registrar(EnlaceSeguimiento enlace);

    Optional<EnlaceSeguimiento> buscarPorToken(String token);

    Optional<EnlaceSeguimiento> buscarActivoPorVenta(Long idVenta);
}
