package co.edu.uco.kudos.cuentas.aplicacion.puertos;

import java.util.Optional;
import java.util.UUID;

public interface Sesiones {

    String abrir(UUID usuarioId);

    Optional<UUID> usuarioDe(String token);

    void cerrar(String token);
}
