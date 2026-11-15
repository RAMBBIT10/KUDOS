package co.edu.uco.kudos.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.catalogos.aplicacion.CatalogoMensajes;
import co.edu.uco.kudos.kudos.aplicacion.NoEncontradoException;
import co.edu.uco.kudos.kudos.aplicacion.ReglaDeNegocioException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ManejadorDeErroresTest {

    private final CatalogoMensajes catalogo = mock(CatalogoMensajes.class);
    private final ManejadorDeErrores manejador = new ManejadorDeErrores(catalogo);

    @Test
    void noEncontradoDevuelve404ConElTextoDelCatalogo() {
        when(catalogo.texto("AUTOR_NO_EXISTE")).thenReturn("El autor no existe");

        ResponseEntity<Map<String, String>> respuesta =
                manejador.noEncontrado(new NoEncontradoException("AUTOR_NO_EXISTE"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).containsEntry("mensaje", "El autor no existe");
    }

    @Test
    void reglaDeNegocioDevuelve400ConElTextoDelCatalogo() {
        when(catalogo.texto("MENSAJE_INVALIDO")).thenReturn("El mensaje no es válido");

        ResponseEntity<Map<String, String>> respuesta =
                manejador.peticionInvalida(new ReglaDeNegocioException("MENSAJE_INVALIDO"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).containsEntry("mensaje", "El mensaje no es válido");
    }

    @Test
    void otroErrorDevuelveElMensajeGenericoDelCatalogo() {
        when(catalogo.texto("PETICION_INVALIDA")).thenReturn("La petición no es válida");

        ResponseEntity<Map<String, String>> respuesta = manejador.peticionInvalida(new IllegalArgumentException("detalle interno"));

        assertThat(respuesta.getBody()).containsEntry("mensaje", "La petición no es válida");
    }
}
