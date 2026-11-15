package co.edu.uco.kudos.notificaciones.infraestructura;

import co.edu.uco.kudos.catalogos.aplicacion.CatalogoNotificaciones;
import co.edu.uco.kudos.catalogos.aplicacion.PlantillaNotificacion;
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
    private final String canal;
    private final CatalogoNotificaciones catalogo;

    public NotificadorCorreo(
            JavaMailSender enviadorDeCorreo,
            ConfiguracionKudos configuracion,
            CatalogoNotificaciones catalogo) {
        this.enviadorDeCorreo = enviadorDeCorreo;
        this.remitente = configuracion.correo().remitente();
        this.canal = configuracion.canales().correo();
        this.catalogo = catalogo;
    }

    @Override
    public String canal() {
        return canal;
    }

    @Override
    public ResultadoEnvio enviar(Usuario destinatario, Kudo kudo) {
        PlantillaNotificacion plantilla = catalogo.plantilla(canal());
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario.correo());
        mensaje.setSubject(plantilla.asunto());
        mensaje.setText(plantilla.cuerpoPara(destinatario.nombre(), kudo.mensaje()));
        enviadorDeCorreo.send(mensaje);
        return ResultadoEnvio.ENVIADO;
    }
}
