package co.edu.uco.kudos.cuentas.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReglasCuentaTest {

    private final ReglasCuenta reglas = new ReglasCuenta("+57", 8);

    @Test
    void aceptaCorreosDeCualquierDominio() {
        assertThat(reglas.correoEsValido("diego.vega1234@uco.net.co")).isTrue();
        assertThat(reglas.correoEsValido("diego@gmail.com")).isTrue();
        assertThat(reglas.correoEsValido("diego_vega@hotmail.com")).isTrue();
        assertThat(reglas.correoEsValido("ramb.bit2406@yahoo.es")).isTrue();
    }

    @Test
    void rechazaCorreosSinFormatoValido() {
        assertThat(reglas.correoEsValido("diego.gmail.com")).isFalse();
        assertThat(reglas.correoEsValido("diego@")).isFalse();
        assertThat(reglas.correoEsValido("@gmail.com")).isFalse();
        assertThat(reglas.correoEsValido("diego@gmail")).isFalse();
        assertThat(reglas.correoEsValido("di ego@gmail.com")).isFalse();
    }

    @Test
    void rechazaCorreosNulosOVacios() {
        assertThat(reglas.correoEsValido(null)).isFalse();
        assertThat(reglas.correoEsValido("")).isFalse();
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
    void validaElLargoMinimoDeLaClave() {
        assertThat(reglas.claveEsValida("12345678")).isTrue();
        assertThat(reglas.claveEsValida("1234567")).isFalse();
        assertThat(reglas.claveEsValida(null)).isFalse();
    }

    @Test
    void validaElNombre() {
        assertThat(reglas.nombreEsValido("Diego Vega")).isTrue();
        assertThat(reglas.nombreEsValido("  ")).isFalse();
        assertThat(reglas.nombreEsValido(null)).isFalse();
    }
}
