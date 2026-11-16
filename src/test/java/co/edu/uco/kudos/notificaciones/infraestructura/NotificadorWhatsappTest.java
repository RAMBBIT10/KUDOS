package co.edu.uco.kudos.notificaciones.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.catalogos.aplicacion.CatalogoNotificaciones;
import co.edu.uco.kudos.catalogos.aplicacion.PlantillaNotificacion;
import co.edu.uco.kudos.compartido.ConfiguracionDePrueba;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import co.edu.uco.kudos.notificaciones.aplicacion.ResultadoEnvio;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificadorWhatsappTest {

    private final Usuario destinatario = new Usuario(UUID.randomUUID(), "Luis", "luis@uco.net.co", "+573000000002");
    private final Kudo kudo = new Kudo(UUID.randomUUID(), UUID.randomUUID(), destinatario.id(), "Buen trabajo", Instant.now(), List.of());

    private final ClienteWhatsapp cliente = mock(ClienteWhatsapp.class);
    private final CatalogoNotificaciones catalogo = mock(CatalogoNotificaciones.class);
    private final NotificadorWhatsapp notificador = new NotificadorWhatsapp(cliente, catalogo, ConfiguracionDePrueba.sinStrapi());

    @Test
    void elCanalSeLlamaWhatsapp() {
        assertThat(notificador.canal()).isEqualTo("whatsapp");
    }

    @Test
    void enviaElMensajeAlTelefonoDelDestinatario() {
        when(catalogo.plantilla("whatsapp")).thenReturn(new PlantillaNotificacion("", "Hola {nombre}: {mensaje}"));

        ResultadoEnvio resultado = notificador.enviar(destinatario, kudo);

        assertThat(resultado).isEqualTo(ResultadoEnvio.ENVIADO);
        verify(cliente).enviar("+573000000002", "Hola Luis: Buen trabajo");
    }
}
