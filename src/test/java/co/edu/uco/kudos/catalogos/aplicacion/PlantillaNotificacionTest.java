package co.edu.uco.kudos.catalogos.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PlantillaNotificacionTest {

    @Test
    void reemplazaElNombreYElMensaje() {
        PlantillaNotificacion plantilla = new PlantillaNotificacion("Asunto", "Hola {nombre}, te dijeron: {mensaje}");

        String cuerpo = plantilla.cuerpoPara("Luis", "Gracias por todo");

        assertThat(cuerpo).isEqualTo("Hola Luis, te dijeron: Gracias por todo");
    }

    @Test
    void dejaElTextoIgualSiNoTieneMarcas() {
        PlantillaNotificacion plantilla = new PlantillaNotificacion(null, "Texto fijo");

        assertThat(plantilla.cuerpoPara("Luis", "Hola")).isEqualTo("Texto fijo");
    }
}
