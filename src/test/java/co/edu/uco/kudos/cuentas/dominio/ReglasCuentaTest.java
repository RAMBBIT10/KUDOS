package co.edu.uco.kudos.cuentas.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReglasCuentaTest {

    private final ReglasCuenta reglas = new ReglasCuenta("uco.net.co", 4);

    @Test
    void aceptaElFormatoInstitucional() {
        assertThat(reglas.correoEsValido("diego.vega1234@uco.net.co")).isTrue();
    }

    @Test
    void rechazaCorreosDeOtroDominio() {
        assertThat(reglas.correoEsValido("diego.vega1234@gmail.com")).isFalse();
    }

    @Test
    void rechazaCorreosSinLosCuatroNumeros() {
        assertThat(reglas.correoEsValido("diego.vega@uco.net.co")).isFalse();
        assertThat(reglas.correoEsValido("diego.vega123@uco.net.co")).isFalse();
        assertThat(reglas.correoEsValido("diego.vega12345@uco.net.co")).isFalse();
    }

    @Test
    void rechazaCorreosNulosOSinPunto() {
        assertThat(reglas.correoEsValido(null)).isFalse();
        assertThat(reglas.correoEsValido("diegovega1234@uco.net.co")).isFalse();
    }

    @Test
    void validaTelefonoConPrefijo() {
        assertThat(reglas.telefonoEsValido("+573001234567")).isTrue();
        assertThat(reglas.telefonoEsValido("3001234567")).isFalse();
        assertThat(reglas.telefonoEsValido(null)).isFalse();
    }

    @Test
    void validaElNombre() {
        assertThat(reglas.nombreEsValido("Diego Vega")).isTrue();
        assertThat(reglas.nombreEsValido("  ")).isFalse();
        assertThat(reglas.nombreEsValido(null)).isFalse();
    }
}
