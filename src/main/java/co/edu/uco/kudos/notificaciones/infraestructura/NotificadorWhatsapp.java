package co.edu.uco.kudos.notificaciones.infraestructura;

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

    public NotificadorWhatsapp(ConfiguracionKudos configuracion) {
        this.datos = configuracion.whatsapp();
        this.cliente = RestClient.builder().baseUrl(datos.urlBase()).build();
    }

    @Override
    public String canal() {
        return "whatsapp";
    }

    @Override
    public ResultadoEnvio enviar(Usuario destinatario, Kudo kudo) {
        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("From", "whatsapp:" + datos.numeroOrigen());
        formulario.add("To", "whatsapp:" + destinatario.telefono());
        formulario.add("Body", "Hola " + destinatario.nombre() + ", recibiste un Kudo: " + kudo.mensaje());

        cliente.post()
                .uri("/2010-04-01/Accounts/{cuenta}/Messages.json", datos.cuentaSid())
                .headers(cabeceras -> cabeceras.setBasicAuth(datos.cuentaSid(), datos.token()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formulario)
                .retrieve()
                .toBodilessEntity();

        return ResultadoEnvio.ENVIADO;
    }
}
