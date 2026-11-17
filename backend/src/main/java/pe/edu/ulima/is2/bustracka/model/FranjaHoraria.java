package pe.edu.ulima.is2.bustracka.model;

public enum FranjaHoraria {
    MANANA(0, 12, "Manana (00:00 a 11:59)"),
    TARDE(12, 18, "Tarde (12:00 a 17:59)"),
    NOCHE(18, 24, "Noche (18:00 a 23:59)");

    private final int horaDesde;
    private final int horaHasta;
    private final String descripcion;

    FranjaHoraria(int horaDesde, int horaHasta, String descripcion) {
        this.horaDesde = horaDesde;
        this.horaHasta = horaHasta;
        this.descripcion = descripcion;
    }

    public int getHoraDesde() {
        return horaDesde;
    }

    public int getHoraHasta() {
        return horaHasta;
    }

    public String getDescripcion() {
        return descripcion;
    }
}