package co.edu.uco.kudos.cuentas.aplicacion;

import co.edu.uco.kudos.cuentas.aplicacion.puertos.AvisosDeCuenta;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.CifradorClaves;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.GeneradorClaves;
import co.edu.uco.kudos.kudos.aplicacion.NoEncontradoException;
import co.edu.uco.kudos.kudos.aplicacion.ReglaDeNegocioException;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResolverSolicitud {

    private final UsuarioRepositorio usuarios;
    private final GeneradorClaves generador;
    private final CifradorClaves cifrador;
    private final AvisosDeCuenta avisos;

    public ResolverSolicitud(
            UsuarioRepositorio usuarios,
            GeneradorClaves generador,
            CifradorClaves cifrador,
            AvisosDeCuenta avisos) {
        this.usuarios = usuarios;
        this.generador = generador;
        this.cifrador = cifrador;
        this.avisos = avisos;
    }

    @Transactional
    public Usuario aprobar(UUID id) {
        buscarPendiente(id);
        String claveTemporal = generador.nueva();
        Usuario aprobado = usuarios.actualizarCuenta(id, EstadoCuenta.ACTIVO, cifrador.cifrar(claveTemporal));
        avisos.cuentaAprobada(aprobado, claveTemporal);
        return aprobado;
    }

    @Transactional
    public Usuario rechazar(UUID id) {
        buscarPendiente(id);
        Usuario rechazado = usuarios.actualizarCuenta(id, EstadoCuenta.RECHAZADO, null);
        avisos.cuentaRechazada(rechazado);
        return rechazado;
    }

    private Usuario buscarPendiente(UUID id) {
        Usuario usuario = usuarios.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoException("SOLICITUD_NO_EXISTE"));
        if (usuario.estado() != EstadoCuenta.PENDIENTE) {
            throw new ReglaDeNegocioException("SOLICITUD_YA_RESUELTA");
        }
        return usuario;
    }
}
