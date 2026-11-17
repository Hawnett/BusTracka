package pe.edu.ulima.is2.bustracka.model;

/** Causa que el administrador elige al registrar el retraso de un viaje (HU 5.2). */
public enum MotivoRetraso {
    TRAFICO("Tráfico en la ruta"),
    CLIMA("Condiciones climáticas"),
    FALLA_MECANICA("Falla mecánica del bus"),
    ACCIDENTE("Accidente en la vía"),
    OTRO("Otro motivo");

    private final String descripcion;

    MotivoRetraso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}