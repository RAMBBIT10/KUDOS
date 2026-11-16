package co.edu.uco.kudos.cuentas.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.cuentas.aplicacion.puertos.AvisosDeCuenta;
import co.edu.uco.kudos.cuentas.dominio.ReglasCuenta;
import co.edu.uco.kudos.kudos.aplicacion.ReglaDeNegocioException;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Rol;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SolicitarCuentaTest {

    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final AvisosDeCuenta avisos = mock(AvisosDeCuenta.class);
    private final SolicitarCuenta casoDeUso = new SolicitarCuenta(usuarios, new ReglasCuenta(List.of("uco.net.co"), 4), avisos);

    private PeticionSolicitudCuenta peticion(String correo, Rol rol) {
        return new PeticionSolicitudCuenta("Diego Vega", correo, "+573001234567", rol);
    }

    @Test
    void guardaLaSolicitudComoPendienteYAvisaAlAdministrador() {
        Usuario guardado = new Usuario(UUID.randomUUID(), "Diego Vega", "diego.vega1234@uco.net.co",
                "+573001234567", Rol.ESTUDIANTE, EstadoCuenta.PENDIENTE);
        when(usuarios.buscarPorCorreo("diego.vega1234@uco.net.co")).thenReturn(Optional.empty());
        when(usuarios.guardar(any(), any(), any(), any(), any(), any())).thenReturn(guardado);

        Usuario resultado = casoDeUso.ejecutar(peticion("Diego.Vega1234@uco.net.co", Rol.ESTUDIANTE));

        assertThat(resultado.estado()).isEqualTo(EstadoCuenta.PENDIENTE);
        verify(usuarios).guardar(eq("Diego Vega"), eq("diego.vega1234@uco.net.co"), eq("+573001234567"),
                eq(Rol.ESTUDIANTE), eq(EstadoCuenta.PENDIENTE), isNull());
        verify(avisos).solicitudRecibida(guardado);
    }

    @Test
    void rechazaUnCorreoQueNoEsInstitucional() {
        assertThatThrownBy(() -> casoDeUso.ejecutar(peticion("diego@gmail.com", Rol.ESTUDIANTE)))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("CORREO_INSTITUCIONAL_INVALIDO");
        verify(usuarios, never()).guardar(any(), any(), any(), any(), any(), any());
    }

    @Test
    void noDejaPedirCuentaDeAdministrador() {
        assertThatThrownBy(() -> casoDeUso.ejecutar(peticion("diego.vega1234@uco.net.co", Rol.ADMINISTRADOR)))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("ROL_INVALIDO");
    }

    @Test
    void rechazaSiYaExisteElCorreo() {
        when(usuarios.buscarPorCorreo("diego.vega1234@uco.net.co"))
                .thenReturn(Optional.of(new Usuario(UUID.randomUUID(), "Diego", "diego.vega1234@uco.net.co", "+573001234567")));

        assertThatThrownBy(() -> casoDeUso.ejecutar(peticion("diego.vega1234@uco.net.co", Rol.PROFESOR)))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("SOLICITUD_YA_EXISTE");
    }
}
