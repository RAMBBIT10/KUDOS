package co.edu.uco.kudos.cuentas.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.cuentas.aplicacion.puertos.CifradorClaves;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.Sesiones;
import co.edu.uco.kudos.kudos.aplicacion.NoAutorizadoException;
import co.edu.uco.kudos.kudos.aplicacion.NoEncontradoException;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Rol;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IniciarSesionTest {

    private final UUID id = UUID.randomUUID();
    private final String correo = "diego.vega1234@uco.net.co";
    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final CifradorClaves cifrador = mock(CifradorClaves.class);
    private final Sesiones sesiones = mock(Sesiones.class);
    private final IniciarSesion casoDeUso = new IniciarSesion(usuarios, cifrador, sesiones);

    private Usuario conEstado(EstadoCuenta estado) {
        return new Usuario(id, "Diego", correo, "+573001234567", Rol.ESTUDIANTE, estado);
    }

    @Test
    void abreSesionConClaveCorrecta() {
        when(usuarios.buscarPorCorreo(correo)).thenReturn(Optional.of(conEstado(EstadoCuenta.ACTIVO)));
        when(usuarios.claveCifradaDe(id)).thenReturn(Optional.of("cifrada"));
        when(cifrador.coincide("miClave", "cifrada")).thenReturn(true);
        when(sesiones.abrir(id)).thenReturn("token123");

        ResultadoSesion resultado = casoDeUso.ejecutar(" Diego.Vega1234@uco.net.co ", "miClave");

        assertThat(resultado.token()).isEqualTo("token123");
        assertThat(resultado.usuario().id()).isEqualTo(id);
    }

    @Test
    void avisaSiNoExisteLaCuenta() {
        when(usuarios.buscarPorCorreo(correo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> casoDeUso.ejecutar(correo, "x"))
                .isInstanceOf(NoEncontradoException.class)
                .hasMessage("CUENTA_NO_EXISTE");
    }

    @Test
    void avisaSiLaSolicitudSigueEnEspera() {
        when(usuarios.buscarPorCorreo(correo)).thenReturn(Optional.of(conEstado(EstadoCuenta.PENDIENTE)));

        assertThatThrownBy(() -> casoDeUso.ejecutar(correo, "x"))
                .isInstanceOf(NoAutorizadoException.class)
                .hasMessage("SOLICITUD_PENDIENTE");
    }

    @Test
    void avisaSiLaSolicitudFueRechazada() {
        when(usuarios.buscarPorCorreo(correo)).thenReturn(Optional.of(conEstado(EstadoCuenta.RECHAZADO)));

        assertThatThrownBy(() -> casoDeUso.ejecutar(correo, "x"))
                .isInstanceOf(NoAutorizadoException.class)
                .hasMessage("SOLICITUD_RECHAZADA");
    }

    @Test
    void rechazaUnaClaveIncorrecta() {
        when(usuarios.buscarPorCorreo(correo)).thenReturn(Optional.of(conEstado(EstadoCuenta.ACTIVO)));
        when(usuarios.claveCifradaDe(id)).thenReturn(Optional.of("cifrada"));
        when(cifrador.coincide("mala", "cifrada")).thenReturn(false);

        assertThatThrownBy(() -> casoDeUso.ejecutar(correo, "mala"))
                .isInstanceOf(NoAutorizadoException.class)
                .hasMessage("CLAVE_INCORRECTA");
    }
}
