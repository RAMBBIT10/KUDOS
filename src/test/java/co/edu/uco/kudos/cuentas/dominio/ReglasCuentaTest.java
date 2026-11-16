package co.edu.uco.kudos.cuentas.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReglasCuentaTest {

    private final ReglasCuenta reglas = new ReglasCuenta(List.of("uco.net.co"), 4, "+57");

    @Test
    void aceptaElFormatoInstitucional() {
        assertThat(reglas.correoEsValido("diego.vega1234@uco.net.co")).isTrue();
    }

    @Test
    void rechazaCorreosDeOtroDominio() {
        assertThat(reglas.correoEsValido("diego.vega1234@gmail.com")).isFalse();
    }

    @Test
    void aceptaVariosDominiosPermitidos() {
        ReglasCuenta variosDominios = new ReglasCuenta(List.of("uco.net.co", "gmail.com", "hotmail.com"), 4, "+57");
        assertThat(variosDominios.correoEsValido("diego.vega1234@gmail.com")).isTrue();
        assertThat(variosDominios.correoEsValido("diego.vega1234@hotmail.com")).isTrue();
        assertThat(variosDominios.correoEsValido("diego.vega1234@yahoo.com")).isFalse();
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
    void validaTelefonoDeDiezNumeros() {
        assertThat(reglas.telefonoEsValido("3001234567")).isTrue();
        assertThat(reglas.telefonoEsValido("300 123-4567")).isTrue();
        assertThat(reglas.telefonoEsValido("+573001234567")).isFalse();
        assertThat(reglas.telefonoEsValido("300123456")).isFalse();
        assertThat(reglas.telefonoEsValido("abc")).isFalse();
        assertThat(reglas.telefonoEsValido(null)).isFalse();
    }

    @Test
    void agregaElPrefijoDelPais() {
        assertThat(reglas.normalizarTelefono("3001234567")).isEqualTo("+573001234567");
        assertThat(reglas.normalizarTelefono("300 123-4567")).isEqualTo("+573001234567");
    }

    @Test
    void validaElNombre() {
        assertThat(reglas.nombreEsValido("Diego Vega")).isTrue();
        assertThat(reglas.nombreEsValido("  ")).isFalse();
        assertThat(reglas.nombreEsValido(null)).isFalse();
    }
}
