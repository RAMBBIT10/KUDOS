package co.edu.uco.kudos.kudos.infraestructura.cola;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.kudos.aplicacion.puertos.PublicadorEventos;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class PublicadorEventosCola implements PublicadorEventos {

    private final StringRedisTemplate redis;
    private final ObjectMapper convertidorJson;
    private final ConfiguracionKudos.Cola cola;

    public PublicadorEventosCola(StringRedisTemplate redis, ObjectMapper convertidorJson, ConfiguracionKudos configuracion) {
        this.redis = redis;
        this.convertidorJson = convertidorJson;
        this.cola = configuracion.cola();
    }

    @Override
    public void kudoPublicado(UUID kudoId) {
        for (String canal : cola.canales()) {
            agregarATrabajo(new TrabajoNotificacion(kudoId, canal, 0));
        }
    }

    void agregarATrabajo(TrabajoNotificacion trabajo) {
        try {
            redis.opsForList().leftPush(cola.nombre(), convertidorJson.writeValueAsString(trabajo));
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("No se pudo convertir el trabajo de notificacion", error);
        }
    }
}
