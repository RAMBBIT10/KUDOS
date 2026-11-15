package co.edu.uco.kudos.catalogos.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.uco.kudos.catalogos.aplicacion.PlantillaNotificacion;
import co.edu.uco.kudos.compartido.ConfiguracionDePrueba;
import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.kudos.aplicacion.NoEncontradoException;
import org.junit.jupiter.api.Test;

class CatalogoNotificacionesStrapiTest {

    private CatalogoNotificacionesStrapi crear(ServidorStrapiFalso servidor) {
        ConfiguracionKudos configuracion = ConfiguracionDePrueba.con(servidor.url(), "");
        return new CatalogoNotificacionesStrapi(new StrapiCliente(configuracion), configuracion);
    }

    @Test
    void devuelveLaPlantillaConAsuntoYCuerpo() {
        try (ServidorStrapiFalso servidor = new ServidorStrapiFalso()) {
            servidor.responder(200, "{\"data\":[{\"canal\":\"correo\",\"asunto\":\"Nuevo Kudo\",\"cuerpo\":\"Hola {nombre}\"}]}");

            PlantillaNotificacion plantilla = crear(servidor).plantilla("correo");

            assertThat(plantilla.asunto()).isEqualTo("Nuevo Kudo");
            assertThat(plantilla.cuerpo()).isEqualTo("Hola {nombre}");
            assertThat(servidor.ultimaConsulta()).isEqualTo("filters[canal][$eq]=correo&pagination[pageSize]=1");
        }
    }

    @Test
    void dejaElAsuntoVacioCuandoElCanalNoTieneAsunto() {
        try (ServidorStrapiFalso servidor = new ServidorStrapiFalso()) {
            servidor.responder(200, "{\"data\":[{\"canal\":\"whatsapp\",\"asunto\":null,\"cuerpo\":\"Hola\"}]}");

            assertThat(crear(servidor).plantilla("whatsapp").asunto()).isEmpty();
        }
    }

    @Test
    void lanzaNoEncontradoSiElCanalNoTienePlantilla() {
        try (ServidorStrapiFalso servidor = new ServidorStrapiFalso()) {
            servidor.responder(200, "{\"data\":[]}");

            assertThatThrownBy(() -> crear(servidor).plantilla("sms"))
                    .isInstanceOf(NoEncontradoException.class)
                    .hasMessage("PLANTILLA_NO_EXISTE");
        }
    }
}
