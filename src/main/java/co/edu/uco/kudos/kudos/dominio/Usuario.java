package co.edu.uco.kudos.kudos.dominio;

import java.util.UUID;

public record Usuario(UUID id, String nombre, String correo, String telefono, Rol rol, EstadoCuenta estado) {

    public Usuario(UUID id, String nombre, String correo, String telefono) {
        this(id, nombre, correo, telefono, Rol.ESTUDIANTE, EstadoCuenta.ACTIVO);
    }

    public boolean estaActivo() {
        return estado == EstadoCuenta.ACTIVO;
    }
}
