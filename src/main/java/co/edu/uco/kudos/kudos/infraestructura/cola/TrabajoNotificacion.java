package co.edu.uco.kudos.kudos.infraestructura.cola;

import java.util.UUID;

public record TrabajoNotificacion(UUID kudoId, String canal, int intento) {
}
