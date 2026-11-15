package co.edu.uco.kudos.notificaciones.infraestructura;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import co.edu.uco.kudos.notificaciones.aplicacion.Notificador;
import co.edu.uco.kudos.notificaciones.aplicacion.ResultadoEnvio;
import org.springframework.stereotype.Component;

@Component
public class NotificadorPush implements Notificador {

    private final ConexionesPush conexiones;
    private final String canal;

    public NotificadorPush(ConexionesPush conexiones, ConfiguracionKudos configuracion) {
        this.conexiones = conexiones;
        this.canal = configuracion.canales().push();
    }

    @Override
    public String canal() {
        return canal;
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
