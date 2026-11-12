package co.edu.uco.kudos.kudos.infraestructura.cola;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.notificaciones.aplicacion.EnviarNotificacion;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProcesadorNotificaciones {

    private static final Logger registro = LoggerFactory.getLogger(ProcesadorNotificaciones.class);
    private static final Duration ESPERA_MAXIMA_DE_LECTURA = Duration.ofSeconds(2);

    private final StringRedisTemplate redis;
    private final ObjectMapper convertidorJson;
    private final EnviarNotificacion enviarNotificacion;
    private final PublicadorEventosCola publicador;
    private final ConfiguracionKudos.Cola cola;
    private final ScheduledExecutorService reintentos = Executors.newSingleThreadScheduledExecutor();

    private volatile boolean activo;
    private Thread hilo;

    public ProcesadorNotificaciones(
            StringRedisTemplate redis,
            ObjectMapper convertidorJson,
            EnviarNotificacion enviarNotificacion,
            PublicadorEventosCola publicador,
            ConfiguracionKudos configuracion) {
        this.redis = redis;
        this.convertidorJson = convertidorJson;
        this.enviarNotificacion = enviarNotificacion;
        this.publicador = publicador;
        this.cola = configuracion.cola();
    }

    @PostConstruct
    void iniciar() {
        activo = true;
        hilo = new Thread(this::escucharCola, "procesador-notificaciones");
        hilo.setDaemon(true);
        hilo.start();
    }

    @PreDestroy
    void detener() {
        activo = false;
        reintentos.shutdownNow();
        hilo.interrupt();
    }

    private void escucharCola() {
        while (activo) {
            try {
                String texto = redis.opsForList().rightPop(cola.nombre(), ESPERA_MAXIMA_DE_LECTURA);
                if (texto != null) {
                    procesar(texto);
                }
            } catch (Exception error) {
                if (activo) {
                    registro.warn("Fallo leyendo la cola de notificaciones: {}", error.getMessage());
                    dormir();
                }
            }
        }
    }

    void procesar(String texto) throws java.io.IOException {
        TrabajoNotificacion trabajo = convertidorJson.readValue(texto, TrabajoNotificacion.class);
        try {
            enviarNotificacion.ejecutar(trabajo.kudoId(), trabajo.canal());
        } catch (Exception error) {
            programarReintento(trabajo);
        }
    }

    private void programarReintento(TrabajoNotificacion trabajo) {
        int siguienteIntento = trabajo.intento() + 1;
        if (siguienteIntento >= cola.intentosMaximos()) {
            registro.warn("Se agotaron los intentos del canal {} para el kudo {}", trabajo.canal(), trabajo.kudoId());
            return;
        }
        long espera = (long) cola.esperaBaseEnSegundos() * (1L << trabajo.intento());
        TrabajoNotificacion nuevo = new TrabajoNotificacion(trabajo.kudoId(), trabajo.canal(), siguienteIntento);
        reintentos.schedule(() -> publicador.agregarATrabajo(nuevo), espera, TimeUnit.SECONDS);
    }

    private void dormir() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
    }
}
