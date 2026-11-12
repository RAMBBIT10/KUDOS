package co.edu.uco.kudos.kudos.aplicacion;

import co.edu.uco.kudos.kudos.dominio.Adjunto;
import java.util.List;
import java.util.UUID;

public record PeticionPublicarKudo(UUID autorId, UUID destinatarioId, String mensaje, List<Adjunto> adjuntos) {
}
