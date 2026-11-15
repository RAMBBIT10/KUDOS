package co.edu.uco.kudos.catalogos.infraestructura;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StrapiCliente {

    private final RestClient cliente;

    public StrapiCliente(ConfiguracionKudos configuracion) {
        ConfiguracionKudos.Strapi datos = configuracion.strapi();
        RestClient.Builder constructor = RestClient.builder().baseUrl(datos.urlBase());
        if (datos.token() != null && !datos.token().isBlank()) {
            constructor.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + datos.token());
        }
        this.cliente = constructor.build();
    }

    public Optional<JsonNode> buscarPor(String ruta, String campo, String valor) {
        JsonNode respuesta = cliente.get()
                .uri(ruta + "?filters[{campo}][$eq]={valor}&pagination[pageSize]=1", campo, valor)
                .retrieve()
                .body(JsonNode.class);

        if (respuesta == null) {
            return Optional.empty();
        }
        JsonNode filas = respuesta.path("data");
        if (!filas.isArray() || filas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(filas.get(0));
    }
}
