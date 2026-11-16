package co.edu.uco.kudos.cuentas.aplicacion;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.cuentas.aplicacion.puertos.CifradorClaves;
import co.edu.uco.kudos.cuentas.dominio.ReglasCuenta;
import co.edu.uco.kudos.kudos.aplicacion.ReglaDeNegocioException;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Rol;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CambiarClaveTest {

    private final UUID id = UUID.randomUUID();
    private final Usuario usuario = new Usuario(id, "Diego", "diego@gmail.com", "+573001234567", Rol.ESTUDIANTE, EstadoCuenta.ACTIVO);
    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final CifradorClaves cifrador = mock(CifradorClaves.class);
    private final CambiarClave casoDeUso = new CambiarClave(usuarios, cifrador, new ReglasCuenta("+57", 8));

    private void conClaveGuardada() {
        when(usuarios.claveCifradaDe(id)).thenReturn(Optional.of("cifrada"));
        when(cifrador.coincide("Temporal123", "cifrada")).thenReturn(true);
    }

    @Test
    void guardaLaClaveNuevaCifrada() {
        conClaveGuardada();
        when(cifrador.cifrar("MiClaveNueva1")).thenReturn("nuevaCifrada");

        casoDeUso.ejecutar(usuario, "Temporal123", "MiClaveNueva1");

        verify(usuarios).actualizarCuenta(id, EstadoCuenta.ACTIVO, "nuevaCifrada");
    }

    @Test
    void rechazaSiLaClaveActualNoCoincide() {
        conClaveGuardada();

        assertThatThrownBy(() -> casoDeUso.ejecutar(usuario, "otra", "MiClaveNueva1"))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("CLAVE_ACTUAL_INCORRECTA");
        verify(usuarios, never()).actualizarCuenta(any(), any(), any());
    }

    @Test
    void rechazaUnaClaveNuevaMuyCorta() {
        conClaveGuardada();

        assertThatThrownBy(() -> casoDeUso.ejecutar(usuario, "Temporal123", "corta"))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("CLAVE_NUEVA_INVALIDA");
        verify(usuarios, never()).actualizarCuenta(any(), any(), any());
    }

    @Test
    void rechazaUnaClaveNuevaIgualALaActual() {
        conClaveGuardada();

        assertThatThrownBy(() -> casoDeUso.ejecutar(usuario, "Temporal123", "Temporal123"))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("CLAVE_NUEVA_IGUAL");
        verify(usuarios, never()).actualizarCuenta(any(), any(), any());
    }
}
