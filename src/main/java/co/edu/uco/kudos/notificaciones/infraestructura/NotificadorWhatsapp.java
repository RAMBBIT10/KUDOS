package co.edu.uco.kudos.notificaciones.infraestructura;

import co.edu.uco.kudos.catalogos.aplicacion.CatalogoNotificaciones;
import co.edu.uco.kudos.catalogos.aplicacion.PlantillaNotificacion;
import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import co.edu.uco.kudos.notificaciones.aplicacion.Notificador;
import co.edu.uco.kudos.notificaciones.aplicacion.ResultadoEnvio;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class NotificadorWhatsapp implements Notificador {

    private final ConfiguracionKudos.Whatsapp datos;
    private final RestClient cliente;
    private final CatalogoNotificaciones catalogo;
    private final String canal;

    public NotificadorWhatsapp(ConfiguracionKudos configuracion, CatalogoNotificaciones catalogo) {
        this.datos = configuracion.whatsapp();
        this.cliente = RestClient.builder().baseUrl(datos.urlBase()).build();
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
        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("From", datos.prefijo() + datos.numeroOrigen());
        formulario.add("To", datos.prefijo() + destinatario.telefono());
        formulario.add("Body", plantilla.cuerpoPara(destinatario.nombre(), kudo.mensaje()));

        cliente.post()
                .uri(datos.rutaEnvio(), datos.cuentaSid())
                .headers(cabeceras -> cabeceras.setBasicAuth(datos.cuentaSid(), datos.token()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formulario)
                .retrieve()
                .toBodilessEntity();

        return ResultadoEnvio.ENVIADO;
    }
}
