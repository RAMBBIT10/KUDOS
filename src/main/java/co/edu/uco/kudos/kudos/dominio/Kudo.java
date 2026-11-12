package co.edu.uco.kudos.kudos.dominio;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Kudo(
        UUID id,
        UUID autorId,
        UUID destinatarioId,
        String mensaje,
        Instant creadoEn,
        List<Adjunto> adjuntos) {
}
