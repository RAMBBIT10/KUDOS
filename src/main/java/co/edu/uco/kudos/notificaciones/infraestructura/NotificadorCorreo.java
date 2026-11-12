package co.edu.uco.kudos.notificaciones.infraestructura;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import co.edu.uco.kudos.notificaciones.aplicacion.Notificador;
import co.edu.uco.kudos.notificaciones.aplicacion.ResultadoEnvio;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class NotificadorCorreo implements Notificador {

    private final JavaMailSender enviadorDeCorreo;
    private final String remitente;

    public NotificadorCorreo(JavaMailSender enviadorDeCorreo, ConfiguracionKudos configuracion) {
        this.enviadorDeCorreo = enviadorDeCorreo;
        this.remitente = configuracion.correo().remitente();
    }

    @Override
    public String canal() {
        return "correo";
    }

    @Override
    public ResultadoEnvio enviar(Usuario destinatario, Kudo kudo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario.correo());
        mensaje.setSubject("Recibiste un Kudo en UCO Kudos");
        mensaje.setText("Hola " + destinatario.nombre() + ", recibiste un reconocimiento:\n\n" + kudo.mensaje());
        enviadorDeCorreo.send(mensaje);
        return ResultadoEnvio.ENVIADO;
    }
}
