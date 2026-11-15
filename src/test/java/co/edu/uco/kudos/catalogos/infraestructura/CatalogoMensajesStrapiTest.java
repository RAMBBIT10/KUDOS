package co.edu.uco.kudos.catalogos.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.uco.kudos.compartido.ConfiguracionDePrueba;
import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import org.junit.jupiter.api.Test;

class CatalogoMensajesStrapiTest {

    private static final String UNA_FILA = "{\"data\":[{\"codigo\":\"AUTOR_NO_EXISTE\",\"texto\":\"El autor no existe\"}]}";

    private CatalogoMensajesStrapi crear(ServidorStrapiFalso servidor) {
        ConfiguracionKudos configuracion = ConfiguracionDePrueba.con(servidor.url(), "");
        return new CatalogoMensajesStrapi(new StrapiCliente(configuracion), configuracion);
    }

    @Test
    void devuelveElTextoQueTraeStrapiParaElCodigo() {
        try (ServidorStrapiFalso servidor = new ServidorStrapiFalso()) {
            servidor.responder(200, UNA_FILA);

            assertThat(crear(servidor).texto("AUTOR_NO_EXISTE")).isEqualTo("El autor no existe");
        }
    }

    @Test
    void devuelveElCodigoSiStrapiNoTieneEseMensaje() {
        try (ServidorStrapiFalso servidor = new ServidorStrapiFalso()) {
            servidor.responder(200, "{\"data\":[]}");

            assertThat(crear(servidor).texto("CODIGO_RARO")).isEqualTo("CODIGO_RARO");
        }
    }

    @Test
    void devuelveElCodigoSiStrapiFalla() {
        try (ServidorStrapiFalso servidor = new ServidorStrapiFalso()) {
            servidor.responder(500, "{}");

            assertThat(crear(servidor).texto("AUTOR_NO_EXISTE")).isEqualTo("AUTOR_NO_EXISTE");
        }
    }

    @Test
    void noVuelveAPreguntarMientrasElTextoEsteEnCache() {
        try (ServidorStrapiFalso servidor = new ServidorStrapiFalso()) {
            servidor.responder(200, UNA_FILA);
            CatalogoMensajesStrapi catalogo = crear(servidor);

            catalogo.texto("AUTOR_NO_EXISTE");
            catalogo.texto("AUTOR_NO_EXISTE");

            assertThat(servidor.peticiones()).isEqualTo(1);
        }
    }
}
