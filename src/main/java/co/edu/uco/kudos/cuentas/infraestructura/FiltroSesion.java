package co.edu.uco.kudos.cuentas.infraestructura;

import co.edu.uco.kudos.cuentas.aplicacion.puertos.Sesiones;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class FiltroSesion extends OncePerRequestFilter {

    private static final String PREFIJO_CABECERA = "Bearer ";
    private static final String PREFIJO_ROL = "ROLE_";
    private static final String RUTA_PUSH = "/push";
    private static final String PARAMETRO_TOKEN = "token";

    private final Sesiones sesiones;
    private final UsuarioRepositorio usuarios;

    public FiltroSesion(Sesiones sesiones, UsuarioRepositorio usuarios) {
        this.sesiones = sesiones;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String token = sacarToken(peticion);
        if (token != null) {
            sesiones.usuarioDe(token)
                    .flatMap(usuarios::buscarPorId)
                    .filter(Usuario::estaActivo)
                    .ifPresent(usuario -> autenticar(usuario, token));
        }
        cadena.doFilter(peticion, respuesta);
    }

    private String sacarToken(HttpServletRequest peticion) {
        String cabecera = peticion.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith(PREFIJO_CABECERA)) {
            return cabecera.substring(PREFIJO_CABECERA.length()).trim();
        }
        if (peticion.getRequestURI().equals(RUTA_PUSH)) {
            return peticion.getParameter(PARAMETRO_TOKEN);
        }
        return null;
    }

    private void autenticar(Usuario usuario, String token) {
        var permisos = List.of(new SimpleGrantedAuthority(PREFIJO_ROL + usuario.rol().name()));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, token, permisos));
    }
}
