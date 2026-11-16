package co.edu.uco.kudos.notificaciones.infraestructura;

import co.edu.uco.kudos.catalogos.aplicacion.CatalogoNotificaciones;
import co.edu.uco.kudos.catalogos.aplicacion.PlantillaNotificacion;
import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import co.edu.uco.kudos.notificaciones.aplicacion.Notificador;
import co.edu.uco.kudos.notificaciones.aplicacion.ResultadoEnvio;
import org.springframework.stereotype.Component;

@Component
public class NotificadorCorreo implements Notificador {

    private final EnviadorCorreo enviador;
    private final CatalogoNotificaciones catalogo;
    private final String canal;

    public NotificadorCorreo(EnviadorCorreo enviador, CatalogoNotificaciones catalogo, ConfiguracionKudos configuracion) {
        this.enviador = enviador;
        this.catalogo = catalogo;
        this.canal = configuracion.canales().correo();
    }

    @Override
    public String canal() {
        return canal;
    }

    @Override
    public ResultadoEnvio enviar(Usuario destinatario, Kudo kudo) {
        PlantillaNotificacion plantilla = catalogo.plantilla(canal());
        enviador.enviar(
                destinatario.correo(),
                plantilla.asunto(),
                plantilla.cuerpoPara(destinatario.nombre(), kudo.mensaje()));
        return ResultadoEnvio.ENVIADO;
    }
}
