package co.edu.uco.kudos.kudos.dominio;

import java.util.List;
import java.util.UUID;

public record DatosNuevoKudo(UUID autorId, UUID destinatarioId, String mensaje, List<Adjunto> adjuntos) {
}
