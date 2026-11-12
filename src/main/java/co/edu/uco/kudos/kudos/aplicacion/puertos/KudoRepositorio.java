package co.edu.uco.kudos.kudos.aplicacion.puertos;

import co.edu.uco.kudos.kudos.dominio.DatosNuevoKudo;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KudoRepositorio {

    Kudo guardar(DatosNuevoKudo datos);

    Optional<Kudo> buscarPorId(UUID id);

    List<Kudo> listarPorDestinatario(UUID destinatarioId, int limite, Instant cursor);
}
