package co.edu.uco.kudos.cuentas.infraestructura;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.Sesiones;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class SesionesRedis implements Sesiones {

    private static final int BYTES_DEL_TOKEN = 32;

    private final StringRedisTemplate redis;
    private final SecureRandom azar = new SecureRandom();
    private final String prefijo;
    private final Duration vida;

    public SesionesRedis(StringRedisTemplate redis, ConfiguracionKudos configuracion) {
        this.redis = redis;
        this.prefijo = configuracion.cuentas().prefijoSesion();
        this.vida = Duration.ofSeconds(configuracion.cuentas().segundosDeSesion());
    }

    @Override
    public String abrir(UUID usuarioId) {
        byte[] bytes = new byte[BYTES_DEL_TOKEN];
        azar.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redis.opsForValue().set(prefijo + token, usuarioId.toString(), vida);
        return token;
    }

    @Override
    public Optional<UUID> usuarioDe(String token) {
        String guardado = redis.opsForValue().get(prefijo + token);
        if (guardado == null) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(guardado));
    }

    @Override
    public void cerrar(String token) {
        redis.delete(prefijo + token);
    }
}
