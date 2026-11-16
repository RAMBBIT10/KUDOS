package co.edu.uco.kudos.cuentas.aplicacion;

import co.edu.uco.kudos.kudos.dominio.Usuario;

public record ResultadoSesion(String token, Usuario usuario) {
}
