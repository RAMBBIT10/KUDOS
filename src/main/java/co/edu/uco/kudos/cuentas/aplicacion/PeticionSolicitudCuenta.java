package co.edu.uco.kudos.cuentas.aplicacion;

import co.edu.uco.kudos.kudos.dominio.Rol;

public record PeticionSolicitudCuenta(String nombre, String correo, String telefono, Rol rol) {
}
