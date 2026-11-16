package co.edu.uco.kudos.cuentas.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CifradorClavesBcryptTest {

    private final CifradorClavesBcrypt cifrador = new CifradorClavesBcrypt();

    @Test
    void laClaveCifradaNoEsLaClaveOriginalYCoincideConElla() {
        String cifrada = cifrador.cifrar("miClave123");

        assertThat(cifrada).isNotEqualTo("miClave123");
        assertThat(cifrador.coincide("miClave123", cifrada)).isTrue();
    }

    @Test
    void unaClaveDistintaNoCoincide() {
        String cifrada = cifrador.cifrar("miClave123");

        assertThat(cifrador.coincide("otraClave", cifrada)).isFalse();
    }
}
