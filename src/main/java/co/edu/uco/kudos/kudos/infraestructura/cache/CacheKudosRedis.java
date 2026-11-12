package co.edu.uco.kudos.kudos.infraestructura.cache;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.kudos.aplicacion.RespuestaListaKudos;
import co.edu.uco.kudos.kudos.aplicacion.puertos.CacheKudos;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CacheKudosRedis implements CacheKudos {

    private static final Logger registro = LoggerFactory.getLogger(CacheKudosRedis.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper convertidorJson;
    private final Duration vida;

    public CacheKudosRedis(StringRedisTemplate redis, ObjectMapper convertidorJson, ConfiguracionKudos configuracion) {
        this.redis = redis;
        this.convertidorJson = convertidorJson;
        this.vida = Duration.ofSeconds(configuracion.cache().segundosDeVida());
    }

    @Override
    public Optional<RespuestaListaKudos> obtener(String clave) {
        String texto = redis.opsForValue().get(clave);
        if (texto == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(convertidorJson.readValue(texto, RespuestaListaKudos.class));
        } catch (JsonProcessingException error) {
            registro.warn("No se pudo leer la cache de la clave {}", clave);
            return Optional.empty();
        }
    }

    @Override
    public void guardar(String clave, RespuestaListaKudos valor) {
        try {
            redis.opsForValue().set(clave, convertidorJson.writeValueAsString(valor), vida);
        } catch (JsonProcessingException error) {
            registro.warn("No se pudo guardar en cache la clave {}", clave);
        }
    }

    @Override
    public void borrarPorPrefijo(String prefijo) {
        Set<String> claves = redis.keys(prefijo + "*");
        if (claves != null && !claves.isEmpty()) {
            redis.delete(claves);
        }
    }
}
