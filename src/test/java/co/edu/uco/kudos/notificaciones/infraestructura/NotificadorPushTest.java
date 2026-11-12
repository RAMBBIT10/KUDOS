package co.edu.uco.kudos.notificaciones.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.kudos.dominio.Kudo;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import co.edu.uco.kudos.notificaciones.aplicacion.ResultadoEnvio;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class NotificadorPushTest {

    private final UUID destinatarioId = UUID.randomUUID();
    private final Usuario destinatario = new Usuario(destinatarioId, "Luis", "luis@uco.edu.co", "+573000000002");
    private final Kudo kudo = new Kudo(UUID.randomUUID(), UUID.randomUUID(), destinatarioId, "Hola", Instant.now(), List.of());

    private final ConexionesPush conexiones = mock(ConexionesPush.class);
    private final NotificadorPush notificador = new NotificadorPush(conexiones);

    @Test
    void omiteElEnvioSiElDestinatarioNoEstaConectado() {
        when(conexiones.estaConectado(destinatarioId)).thenReturn(false);

        ResultadoEnvio resultado = notificador.enviar(destinatario, kudo);

        assertThat(resultado).isEqualTo(ResultadoEnvio.OMITIDO);
        verify(conexiones, never()).enviarAlUsuario(any(), anyString(), any());
    }

    @Test
    void enviaElKudoSiElDestinatarioEstaConectado() {
        when(conexiones.estaConectado(destinatarioId)).thenReturn(true);

        ResultadoEnvio resultado = notificador.enviar(destinatario, kudo);

        assertThat(resultado).isEqualTo(ResultadoEnvio.ENVIADO);
        verify(conexiones).enviarAlUsuario(destinatarioId, "kudo-recibido", kudo);
    }

    @Test
    void elCanalSeLlamaPush() {
        assertThat(notificador.canal()).isEqualTo("push");
    }

    @Test
    void conexionesMarcaComoConectadoAlRegistrarYComoDesconectadoAlRetirar() {
        ConexionesPush conexionesReales = new ConexionesPush();

        SseEmitter canal = conexionesReales.registrar(destinatarioId);
        assertThat(conexionesReales.estaConectado(destinatarioId)).isTrue();

        conexionesReales.retirar(destinatarioId, canal);
        assertThat(conexionesReales.estaConectado(destinatarioId)).isFalse();
    }

    @Test
    void conexionesSigueConectadoSiQuedaOtroCanalDelMismoUsuario() {
        ConexionesPush conexionesReales = new ConexionesPush();
        SseEmitter primero = conexionesReales.registrar(destinatarioId);
        conexionesReales.registrar(destinatarioId);

        conexionesReales.retirar(destinatarioId, primero);

        assertThat(conexionesReales.estaConectado(destinatarioId)).isTrue();
    }
}
