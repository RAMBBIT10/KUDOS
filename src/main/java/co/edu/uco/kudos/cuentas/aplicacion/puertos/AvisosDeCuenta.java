package co.edu.uco.kudos.cuentas.aplicacion.puertos;

import co.edu.uco.kudos.kudos.dominio.Usuario;

public interface AvisosDeCuenta {

    void solicitudRecibida(Usuario solicitante);

    void cuentaAprobada(Usuario usuario, String claveTemporal);

    void cuentaRechazada(Usuario usuario);
}
