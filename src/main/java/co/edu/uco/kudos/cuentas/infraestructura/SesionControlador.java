package co.edu.uco.kudos.cuentas.infraestructura;

import co.edu.uco.kudos.cuentas.aplicacion.IniciarSesion;
import co.edu.uco.kudos.cuentas.aplicacion.ResultadoSesion;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.Sesiones;
import co.edu.uco.kudos.kudos.dominio.Rol;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sesion")
public class SesionControlador {

    public record PeticionIngreso(String correo, String clave) {
    }

    public record UsuarioDeSesion(UUID id, String nombre, String correo, Rol rol) {
    }

    public record RespuestaIngreso(String token, UsuarioDeSesion usuario) {
    }

    private final IniciarSesion iniciarSesion;
    private final Sesiones sesiones;

    public SesionControlador(IniciarSesion iniciarSesion, Sesiones sesiones) {
        this.iniciarSesion = iniciarSesion;
        this.sesiones = sesiones;
    }

    @PostMapping
    public RespuestaIngreso ingresar(@RequestBody PeticionIngreso peticion) {
        ResultadoSesion resultado = iniciarSesion.ejecutar(peticion.correo(), peticion.clave());
        return new RespuestaIngreso(resultado.token(), convertir(resultado.usuario()));
    }

    @GetMapping
    public UsuarioDeSesion actual(@AuthenticationPrincipal Usuario usuario) {
        return convertir(usuario);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void salir(Authentication autenticacion) {
        sesiones.cerrar((String) autenticacion.getCredentials());
    }

    private UsuarioDeSesion convertir(Usuario usuario) {
        return new UsuarioDeSesion(usuario.id(), usuario.nombre(), usuario.correo(), usuario.rol());
    }
}
