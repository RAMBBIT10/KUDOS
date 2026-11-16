package co.edu.uco.kudos.cuentas.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.uco.kudos.compartido.ConfiguracionDePrueba;
import org.junit.jupiter.api.Test;

class GeneradorClavesSegurasTest {

    private final GeneradorClavesSeguras generador = new GeneradorClavesSeguras(ConfiguracionDePrueba.sinStrapi());

    @Test
    void generaClavesConLaLongitudConfigurada() {
        assertThat(generador.nueva()).hasSize(10);
    }

    @Test
    void generaClavesDistintasEnCadaLlamada() {
        assertThat(generador.nueva()).isNotEqualTo(generador.nueva());
    }

    @Test
    void usaSoloLetrasYNumerosSinCaracteresConfusos() {
        assertThat(generador.nueva()).matches("[abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789]+");
    }
}
