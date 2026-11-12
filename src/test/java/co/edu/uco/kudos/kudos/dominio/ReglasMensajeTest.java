package co.edu.uco.kudos.kudos.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReglasMensajeTest {

    @Test
    void aceptaUnMensajeNormal() {
        assertThat(ReglasMensaje.mensajeEsValido("Gracias por tu ayuda")).isTrue();
    }

    @Test
    void rechazaUnMensajeVacioONulo() {
        assertThat(ReglasMensaje.mensajeEsValido("")).isFalse();
        assertThat(ReglasMensaje.mensajeEsValido("     ")).isFalse();
        assertThat(ReglasMensaje.mensajeEsValido(null)).isFalse();
    }

    @Test
    void aceptaUnMensajeJustoEnElLimite() {
        String mensaje = "a".repeat(ReglasMensaje.LIMITE_CARACTERES);
        assertThat(ReglasMensaje.mensajeEsValido(mensaje)).isTrue();
    }

    @Test
    void rechazaUnMensajeQuePasaElLimite() {
        String mensaje = "a".repeat(ReglasMensaje.LIMITE_CARACTERES + 1);
        assertThat(ReglasMensaje.mensajeEsValido(mensaje)).isFalse();
    }
}
