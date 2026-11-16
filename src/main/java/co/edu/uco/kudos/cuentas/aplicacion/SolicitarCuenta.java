package co.edu.uco.kudos.cuentas.aplicacion;

import co.edu.uco.kudos.cuentas.aplicacion.puertos.AvisosDeCuenta;
import co.edu.uco.kudos.cuentas.dominio.ReglasCuenta;
import co.edu.uco.kudos.kudos.aplicacion.ReglaDeNegocioException;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Rol;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import org.springframework.stereotype.Service;

@Service
public class SolicitarCuenta {

    private final UsuarioRepositorio usuarios;
    private final ReglasCuenta reglas;
    private final AvisosDeCuenta avisos;

    public SolicitarCuenta(UsuarioRepositorio usuarios, ReglasCuenta reglas, AvisosDeCuenta avisos) {
        this.usuarios = usuarios;
        this.reglas = reglas;
        this.avisos = avisos;
    }

    public Usuario ejecutar(PeticionSolicitudCuenta peticion) {
        if (!reglas.nombreEsValido(peticion.nombre())) {
            throw new ReglaDeNegocioException("NOMBRE_INVALIDO");
        }

        String correo = peticion.correo() == null ? null : peticion.correo().trim().toLowerCase();
        if (!reglas.correoEsValido(correo)) {
            throw new ReglaDeNegocioException("CORREO_INSTITUCIONAL_INVALIDO");
        }

        if (!reglas.telefonoEsValido(peticion.telefono())) {
            throw new ReglaDeNegocioException("TELEFONO_INVALIDO");
        }
        String telefono = reglas.normalizarTelefono(peticion.telefono());

        if (peticion.rol() == null || peticion.rol() == Rol.ADMINISTRADOR) {
            throw new ReglaDeNegocioException("ROL_INVALIDO");
        }

        if (usuarios.buscarPorCorreo(correo).isPresent()) {
            throw new ReglaDeNegocioException("SOLICITUD_YA_EXISTE");
        }

        Usuario solicitante = usuarios.guardar(
                peticion.nombre().trim(), correo, telefono, peticion.rol(), EstadoCuenta.PENDIENTE, null);
        avisos.solicitudRecibida(solicitante);
        return solicitante;
    }
}
