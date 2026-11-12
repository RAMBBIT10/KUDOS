package co.edu.uco.kudos.kudos.dominio;

import java.util.UUID;

public record Usuario(UUID id, String nombre, String correo, String telefono) {
}
