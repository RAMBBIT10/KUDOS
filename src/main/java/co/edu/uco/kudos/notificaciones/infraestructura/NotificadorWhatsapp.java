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
public class NotificadorWhatsapp implements Notificador {

    private final ClienteWhatsapp cliente;
    private final CatalogoNotificaciones catalogo;
    private final String canal;

    public NotificadorWhatsapp(ClienteWhatsapp cliente, CatalogoNotificaciones catalogo, ConfiguracionKudos configuracion) {
        this.cliente = cliente;
        this.catalogo = catalogo;
        this.canal = configuracion.canales().whatsapp();
    }

    @Override
    public String canal() {
        return canal;
    }

    @Override
    public ResultadoEnvio enviar(Usuario destinatario, Kudo kudo) {
        PlantillaNotificacion plantilla = catalogo.plantilla(canal());
        cliente.enviar(destinatario.telefono(), plantilla.cuerpoPara(destinatario.nombre(), kudo.mensaje()));
        return ResultadoEnvio.ENVIADO;
    }
}
