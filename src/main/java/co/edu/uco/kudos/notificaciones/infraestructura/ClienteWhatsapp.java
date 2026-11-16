package co.edu.uco.kudos.notificaciones.infraestructura;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class ClienteWhatsapp {

    private final ConfiguracionKudos.Whatsapp datos;
    private final RestClient cliente;

    public ClienteWhatsapp(ConfiguracionKudos configuracion) {
        this.datos = configuracion.whatsapp();
        this.cliente = RestClient.builder().baseUrl(datos.urlBase()).build();
    }

    public void enviar(String telefono, String texto) {
        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("From", datos.prefijo() + datos.numeroOrigen());
        formulario.add("To", datos.prefijo() + telefono);
        formulario.add("Body", texto);

        cliente.post()
                .uri(datos.rutaEnvio(), datos.cuentaSid())
                .headers(cabeceras -> cabeceras.setBasicAuth(datos.cuentaSid(), datos.token()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formulario)
                .retrieve()
                .toBodilessEntity();
    }
}
