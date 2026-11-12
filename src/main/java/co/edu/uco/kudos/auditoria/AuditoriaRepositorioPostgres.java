package co.edu.uco.kudos.auditoria;

import co.edu.uco.kudos.kudos.aplicacion.puertos.AuditoriaRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoAuditoria;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class AuditoriaRepositorioPostgres implements AuditoriaRepositorio {

    private static final int LARGO_MAXIMO_DETALLE = 1000;

    private final AuditoriaJpa tabla;

    public AuditoriaRepositorioPostgres(AuditoriaJpa tabla) {
        this.tabla = tabla;
    }

    @Override
    public void registrar(UUID kudoId, String evento, String canal, EstadoAuditoria estado, String detalle) {
        AuditoriaEntidad entidad = new AuditoriaEntidad();
        entidad.setKudoId(kudoId);
        entidad.setEvento(evento);
        entidad.setCanal(canal);
        entidad.setEstado(estado);
        entidad.setDetalle(recortar(detalle));
        entidad.setCreadoEn(Instant.now().truncatedTo(ChronoUnit.MICROS));
        tabla.save(entidad);
    }

    private String recortar(String detalle) {
        if (detalle == null) {
            return "";
        }
        return detalle.length() > LARGO_MAXIMO_DETALLE ? detalle.substring(0, LARGO_MAXIMO_DETALLE) : detalle;
    }
}
