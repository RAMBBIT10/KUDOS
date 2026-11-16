package co.edu.uco.kudos.notificaciones.infraestructura;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EnviadorCorreo {

    private final JavaMailSender enviadorDeCorreo;
    private final String remitente;

    public EnviadorCorreo(JavaMailSender enviadorDeCorreo, ConfiguracionKudos configuracion) {
        this.enviadorDeCorreo = enviadorDeCorreo;
        this.remitente = configuracion.correo().remitente();
    }

    public void enviar(String destinatario, String asunto, String texto) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(texto);
        enviadorDeCorreo.send(mensaje);
    }
}
