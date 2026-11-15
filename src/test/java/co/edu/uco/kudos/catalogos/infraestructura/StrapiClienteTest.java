package co.edu.uco.kudos.catalogos.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.uco.kudos.compartido.ConfiguracionDePrueba;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StrapiClienteTest {

    private static final String UNA_FILA = "{\"data\":[{\"id\":1,\"codigo\":\"AUTOR_NO_EXISTE\",\"texto\":\"El autor no existe\"}]}";

    @Test
    void pideLaFilaPorElCampoYDevuelveLaPrimera() {
        try (ServidorStrapiFalso servidor = new ServidorStrapiFalso()) {
            servidor.responder(200, UNA_FILA);
            StrapiCliente cliente = new StrapiCliente(ConfiguracionDePrueba.con(servidor.url(), ""));

            Optional<JsonNode> fila = cliente.buscarPor("/api/mensajes", "codigo", "AUTOR_NO_EXISTE");

            assertThat(fila).isPresent();
            assertThat(fila.get().path("texto").asText()).isEqualTo("El autor no existe");
            assertThat(servidor.ultimaRuta()).isEqualTo("/api/mensajes");
            assertThat(servidor.ultimaConsulta())
                    .isEqualTo("filters[codigo][$eq]=AUTOR_NO_EXISTE&pagination[pageSize]=1");
        }
    }

    @Test
    void devuelveVacioCuandoStrapiNoTrajoFilas() {
        try (ServidorStrapiFalso servidor = new ServidorStrapiFalso()) {
            servidor.responder(200, "{\"data\":[]}");
            StrapiCliente cliente = new StrapiCliente(ConfiguracionDePrueba.con(servidor.url(), ""));

            assertThat(cliente.buscarPor("/api/mensajes", "codigo", "NO_EXISTE")).isEmpty();
        }
    }

    @Test
    void mandaElTokenSoloCuandoEstaConfigurado() {
        try (ServidorStrapiFalso servidor = new ServidorStrapiFalso()) {
            servidor.responder(200, UNA_FILA);

            new StrapiCliente(ConfiguracionDePrueba.con(servidor.url(), "")).buscarPor("/api/mensajes", "codigo", "A");
            assertThat(servidor.ultimaAutorizacion()).isEqualTo("null");

            new StrapiCliente(ConfiguracionDePrueba.con(servidor.url(), "secreto")).buscarPor("/api/mensajes", "codigo", "A");
            assertThat(servidor.ultimaAutorizacion()).isEqualTo("Bearer secreto");
        }
    }
}
