package co.edu.uco.kudos.cuentas.aplicacion;

import co.edu.uco.kudos.cuentas.aplicacion.puertos.CifradorClaves;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.Sesiones;
import co.edu.uco.kudos.kudos.aplicacion.NoAutorizadoException;
import co.edu.uco.kudos.kudos.aplicacion.NoEncontradoException;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import org.springframework.stereotype.Service;

@Service
public class IniciarSesion {

    private final UsuarioRepositorio usuarios;
    private final CifradorClaves cifrador;
    private final Sesiones sesiones;

    public IniciarSesion(UsuarioRepositorio usuarios, CifradorClaves cifrador, Sesiones sesiones) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.sesiones = sesiones;
    }

    public ResultadoSesion ejecutar(String correo, String clave) {
        String correoNormalizado = correo == null ? "" : correo.trim().toLowerCase();
        Usuario usuario = usuarios.buscarPorCorreo(correoNormalizado)
                .orElseThrow(() -> new NoEncontradoException("CUENTA_NO_EXISTE"));

        if (usuario.estado() == EstadoCuenta.PENDIENTE) {
            throw new NoAutorizadoException("SOLICITUD_PENDIENTE");
        }
        if (usuario.estado() == EstadoCuenta.RECHAZADO) {
            throw new NoAutorizadoException("SOLICITUD_RECHAZADA");
        }

        String claveCifrada = usuarios.claveCifradaDe(usuario.id())
                .orElseThrow(() -> new NoAutorizadoException("CLAVE_INCORRECTA"));
        if (clave == null || !cifrador.coincide(clave, claveCifrada)) {
            throw new NoAutorizadoException("CLAVE_INCORRECTA");
        }

        return new ResultadoSesion(sesiones.abrir(usuario.id()), usuario);
    }
}
