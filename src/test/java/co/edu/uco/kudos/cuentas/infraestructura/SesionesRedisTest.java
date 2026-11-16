package co.edu.uco.kudos.cuentas.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.compartido.ConfiguracionDePrueba;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class SesionesRedisTest {

    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valores = mock(ValueOperations.class);
    private final SesionesRedis sesiones = new SesionesRedis(redis, ConfiguracionDePrueba.sinStrapi());

    SesionesRedisTest() {
        when(redis.opsForValue()).thenReturn(valores);
    }

    @Test
    void abrirGuardaElUsuarioConElPrefijoYLaVidaConfigurada() {
        UUID usuarioId = UUID.randomUUID();

        String token = sesiones.abrir(usuarioId);

        assertThat(token).isNotBlank();
        verify(valores).set("sesion:" + token, usuarioId.toString(), Duration.ofSeconds(3600));
    }

    @Test
    void abrirEntregaTokensDistintos() {
        assertThat(sesiones.abrir(UUID.randomUUID())).isNotEqualTo(sesiones.abrir(UUID.randomUUID()));
    }

    @Test
    void usuarioDeDevuelveElUsuarioGuardado() {
        UUID usuarioId = UUID.randomUUID();
        when(valores.get("sesion:abc")).thenReturn(usuarioId.toString());

        assertThat(sesiones.usuarioDe("abc")).isEqualTo(Optional.of(usuarioId));
    }

    @Test
    void usuarioDeDevuelveVacioSiElTokenNoExiste() {
        when(valores.get(anyString())).thenReturn(null);

        assertThat(sesiones.usuarioDe("nada")).isEmpty();
    }

    @Test
    void cerrarBorraLaSesion() {
        sesiones.cerrar("abc");

        verify(redis).delete("sesion:abc");
    }
}
