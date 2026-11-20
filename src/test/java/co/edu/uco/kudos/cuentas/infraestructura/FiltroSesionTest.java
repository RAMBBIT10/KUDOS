package co.edu.uco.kudos.cuentas.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.cuentas.aplicacion.puertos.Sesiones;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Rol;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import jakarta.servlet.FilterChain;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class FiltroSesionTest {

    private final UUID usuarioId = UUID.randomUUID();
    private final Sesiones sesiones = mock(Sesiones.class);
    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final FilterChain cadena = mock(FilterChain.class);
    private final FiltroSesion filtro = new FiltroSesion(sesiones, usuarios);
    private final MockHttpServletResponse respuesta = new MockHttpServletResponse();

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private Usuario usuarioConEstado(EstadoCuenta estado) {
        return new Usuario(usuarioId, "Luis", "luis@uco.net.co", "+573000000002", Rol.PROFESOR, estado);
    }

    @Test
    void autenticaConElTokenDeLaCabecera() throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/kudos");
        peticion.addHeader("Authorization", "Bearer abc");
        when(sesiones.usuarioDe("abc")).thenReturn(Optional.of(usuarioId));
        when(usuarios.buscarPorId(usuarioId)).thenReturn(Optional.of(usuarioConEstado(EstadoCuenta.ACTIVO)));

        filtro.doFilter(peticion, respuesta, cadena);

        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        assertThat(autenticacion).isNotNull();
        assertThat(autenticacion.getCredentials()).isEqualTo("abc");
        assertThat(autenticacion.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_PROFESOR");
        verify(cadena).doFilter(peticion, respuesta);
    }

    @Test
    void sinCabeceraNoAutenticaPeroDejaPasar() throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/kudos");

        filtro.doFilter(peticion, respuesta, cadena);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(cadena).doFilter(peticion, respuesta);
    }

    @Test
    void unTokenDesconocidoNoAutentica() throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/kudos");
        peticion.addHeader("Authorization", "Bearer falso");
        when(sesiones.usuarioDe("falso")).thenReturn(Optional.empty());

        filtro.doFilter(peticion, respuesta, cadena);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void unUsuarioNoActivoNoAutentica() throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/kudos");
        peticion.addHeader("Authorization", "Bearer abc");
        when(sesiones.usuarioDe("abc")).thenReturn(Optional.of(usuarioId));
        when(usuarios.buscarPorId(usuarioId)).thenReturn(Optional.of(usuarioConEstado(EstadoCuenta.RECHAZADO)));

        filtro.doFilter(peticion, respuesta, cadena);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void enLaRutaPushAceptaElTokenPorParametro() throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/push");
        peticion.setParameter("token", "abc");
        when(sesiones.usuarioDe("abc")).thenReturn(Optional.of(usuarioId));
        when(usuarios.buscarPorId(usuarioId)).thenReturn(Optional.of(usuarioConEstado(EstadoCuenta.ACTIVO)));

        filtro.doFilter(peticion, respuesta, cadena);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void fueraDeLaRutaPushElParametroTokenSeIgnora() throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/kudos");
        peticion.setParameter("token", "abc");

        filtro.doFilter(peticion, respuesta, cadena);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
