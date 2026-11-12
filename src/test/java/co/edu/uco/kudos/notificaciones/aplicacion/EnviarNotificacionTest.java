package co.edu.uco.kudos.notificaciones.aplicacion;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.kudos.aplicacion.NoEncontradoException;
import co.edu.uco.kudos.kudos.aplicacion.puertos.AuditoriaRepositorio;
import co.edu.uco.kudos.kudos.aplicacion.puertos.KudoRepositorio;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoAuditoria;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnviarNotificacionTest {

    private final UUID kudoId = UUID.randomUUID();
    private final UUID destinatarioId = UUID.randomUUID();

    private final Notificador correo = mock(Notificador.class);
    private final KudoRepositorio kudos = mock(KudoRepositorio.class);
    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final AuditoriaRepositorio auditoria = mock(AuditoriaRepositorio.class);

    private final EnviarNotificacion casoDeUso = new EnviarNotificacion(List.of(correo), kudos, usuarios, auditoria);

    @BeforeEach
    void preparar() {
        when(correo.canal()).thenReturn("correo");
        Kudo kudo = new Kudo(kudoId, UUID.randomUUID(), destinatarioId, "Hola", Instant.now(), List.of());
        when(kudos.buscarPorId(kudoId)).thenReturn(Optional.of(kudo));
        when(usuarios.buscarPorId(destinatarioId))
                .thenReturn(Optional.of(new Usuario(destinatarioId, "Luis", "luis@uco.edu.co", "+573000000002")));
    }

    @Test
    void registraOkCuandoElCanalEnvia() {
        when(correo.enviar(any(), any())).thenReturn(ResultadoEnvio.ENVIADO);

        casoDeUso.ejecutar(kudoId, "correo");

        verify(auditoria).registrar(eq(kudoId), eq("NOTIFICACION"), eq("correo"), eq(EstadoAuditoria.OK), anyString());
    }

    @Test
    void registraOmitidoCuandoElDestinatarioNoEstaConectado() {
        when(correo.enviar(any(), any())).thenReturn(ResultadoEnvio.OMITIDO);

        casoDeUso.ejecutar(kudoId, "correo");

        verify(auditoria).registrar(eq(kudoId), eq("NOTIFICACION"), eq("correo"), eq(EstadoAuditoria.OMITIDO), anyString());
    }

    @Test
    void registraErrorYVuelveALanzarParaQueLaColaReintente() {
        when(correo.enviar(any(), any())).thenThrow(new IllegalStateException("Servidor caido"));

        assertThatThrownBy(() -> casoDeUso.ejecutar(kudoId, "correo"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Servidor caido");

        verify(auditoria).registrar(kudoId, "NOTIFICACION", "correo", EstadoAuditoria.ERROR, "Servidor caido");
    }

    @Test
    void fallaSiElCanalNoExiste() {
        assertThatThrownBy(() -> casoDeUso.ejecutar(kudoId, "telegram")).isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void fallaSiElKudoNoExiste() {
        UUID otroKudo = UUID.randomUUID();
        when(kudos.buscarPorId(otroKudo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> casoDeUso.ejecutar(otroKudo, "correo")).isInstanceOf(NoEncontradoException.class);
    }
}
