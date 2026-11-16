package co.edu.uco.kudos.cuentas.aplicacion;

import co.edu.uco.kudos.cuentas.aplicacion.puertos.CifradorClaves;
import co.edu.uco.kudos.cuentas.dominio.ReglasCuenta;
import co.edu.uco.kudos.kudos.aplicacion.ReglaDeNegocioException;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import org.springframework.stereotype.Service;

@Service
public class CambiarClave {

    private final UsuarioRepositorio usuarios;
    private final CifradorClaves cifrador;
    private final ReglasCuenta reglas;

    public CambiarClave(UsuarioRepositorio usuarios, CifradorClaves cifrador, ReglasCuenta reglas) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.reglas = reglas;
    }

    public void ejecutar(Usuario usuario, String claveActual, String claveNueva) {
        String claveGuardada = usuarios.claveCifradaDe(usuario.id())
                .orElseThrow(() -> new ReglaDeNegocioException("CLAVE_ACTUAL_INCORRECTA"));
        if (claveActual == null || !cifrador.coincide(claveActual, claveGuardada)) {
            throw new ReglaDeNegocioException("CLAVE_ACTUAL_INCORRECTA");
        }
        if (!reglas.claveEsValida(claveNueva)) {
            throw new ReglaDeNegocioException("CLAVE_NUEVA_INVALIDA");
        }
        if (claveNueva.equals(claveActual)) {
            throw new ReglaDeNegocioException("CLAVE_NUEVA_IGUAL");
        }
        usuarios.actualizarCuenta(usuario.id(), usuario.estado(), cifrador.cifrar(claveNueva));
    }
}
