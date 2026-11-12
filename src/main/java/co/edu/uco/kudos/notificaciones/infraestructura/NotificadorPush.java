package co.edu.uco.kudos.notificaciones.infraestructura;

import co.edu.uco.kudos.kudos.dominio.Kudo;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import co.edu.uco.kudos.notificaciones.aplicacion.Notificador;
import co.edu.uco.kudos.notificaciones.aplicacion.ResultadoEnvio;
import org.springframework.stereotype.Component;

@Component
public class NotificadorPush implements Notificador {

    private final ConexionesPush conexiones;

    public NotificadorPush(ConexionesPush conexiones) {
        this.conexiones = conexiones;
    }

    @Override
    public String canal() {
        return "push";
    }

    @Override
    public ResultadoEnvio enviar(Usuario destinatario, Kudo kudo) {
        if (!conexiones.estaConectado(destinatario.id())) {
            return ResultadoEnvio.OMITIDO;
        }
        conexiones.enviarAlUsuario(destinatario.id(), "kudo-recibido", kudo);
        return ResultadoEnvio.ENVIADO;
    }
}
