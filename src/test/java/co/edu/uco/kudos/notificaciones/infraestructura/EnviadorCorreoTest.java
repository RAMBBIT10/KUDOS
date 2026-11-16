package co.edu.uco.kudos.notificaciones.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class EnviadorCorreoTest {

    private final JavaMailSender servidorDeCorreo = mock(JavaMailSender.class);

    @Test
    void armaElMensajeConRemitenteDestinatarioAsuntoYTexto() {
        ConfiguracionKudos configuracion = new ConfiguracionKudos(
                null, null, new ConfiguracionKudos.Correo("kudos@uco.net.co"), null, null, null, null, null, null);
        EnviadorCorreo enviador = new EnviadorCorreo(servidorDeCorreo, configuracion);

        enviador.enviar("luis@uco.net.co", "Asunto", "Texto");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(servidorDeCorreo).send(captor.capture());
        SimpleMailMessage mensaje = captor.getValue();
        assertThat(mensaje.getFrom()).isEqualTo("kudos@uco.net.co");
        assertThat(mensaje.getTo()).containsExactly("luis@uco.net.co");
        assertThat(mensaje.getSubject()).isEqualTo("Asunto");
        assertThat(mensaje.getText()).isEqualTo("Texto");
    }
}
