package co.edu.uco.kudos.cuentas.aplicacion;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.cuentas.aplicacion.puertos.AvisosDeCuenta;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.CifradorClaves;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.GeneradorClaves;
import co.edu.uco.kudos.kudos.aplicacion.NoEncontradoException;
import co.edu.uco.kudos.kudos.aplicacion.ReglaDeNegocioException;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Rol;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ResolverSolicitudTest {

    private final UUID id = UUID.randomUUID();
    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final GeneradorClaves generador = mock(GeneradorClaves.class);
    private final CifradorClaves cifrador = mock(CifradorClaves.class);
    private final AvisosDeCuenta avisos = mock(AvisosDeCuenta.class);
    private final ResolverSolicitud casoDeUso = new ResolverSolicitud(usuarios, generador, cifrador, avisos);

    private Usuario conEstado(EstadoCuenta estado) {
        return new Usuario(id, "Diego", "diego.vega1234@uco.net.co", "+573001234567", Rol.ESTUDIANTE, estado);
    }

    @Test
    void aprobarActivaLaCuentaConClaveCifradaYAvisaConLaClaveTemporal() {
        Usuario activo = conEstado(EstadoCuenta.ACTIVO);
        when(usuarios.buscarPorId(id)).thenReturn(Optional.of(conEstado(EstadoCuenta.PENDIENTE)));
        when(generador.nueva()).thenReturn("Temporal123");
        when(cifrador.cifrar("Temporal123")).thenReturn("cifrada");
        when(usuarios.actualizarCuenta(id, EstadoCuenta.ACTIVO, "cifrada")).thenReturn(activo);

        casoDeUso.aprobar(id);

        verify(avisos).cuentaAprobada(activo, "Temporal123");
    }

    @Test
    void rechazarMarcaLaSolicitudYAvisa() {
        Usuario rechazado = conEstado(EstadoCuenta.RECHAZADO);
        when(usuarios.buscarPorId(id)).thenReturn(Optional.of(conEstado(EstadoCuenta.PENDIENTE)));
        when(usuarios.actualizarCuenta(eq(id), eq(EstadoCuenta.RECHAZADO), isNull())).thenReturn(rechazado);

        casoDeUso.rechazar(id);

        verify(avisos).cuentaRechazada(rechazado);
    }

    @Test
    void noResuelveUnaSolicitudQueYaFueResuelta() {
        when(usuarios.buscarPorId(id)).thenReturn(Optional.of(conEstado(EstadoCuenta.ACTIVO)));

        assertThatThrownBy(() -> casoDeUso.aprobar(id))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("SOLICITUD_YA_RESUELTA");
    }

    @Test
    void fallaSiLaSolicitudNoExiste() {
        when(usuarios.buscarPorId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> casoDeUso.rechazar(UUID.randomUUID()))
                .isInstanceOf(NoEncontradoException.class)
                .hasMessage("SOLICITUD_NO_EXISTE");
    }
}
