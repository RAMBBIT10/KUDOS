package co.edu.uco.kudos.kudos.aplicacion.puertos;

import co.edu.uco.kudos.kudos.dominio.EstadoAuditoria;
import java.util.UUID;

public interface AuditoriaRepositorio {

    void registrar(UUID kudoId, String evento, String canal, EstadoAuditoria estado, String detalle);
}
