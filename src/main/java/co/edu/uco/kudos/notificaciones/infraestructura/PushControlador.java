package co.edu.uco.kudos.notificaciones.infraestructura;

import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class PushControlador {

    private final ConexionesPush conexiones;

    public PushControlador(ConexionesPush conexiones) {
        this.conexiones = conexiones;
    }

    @GetMapping(path = "/push/{usuarioId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter conectar(@PathVariable UUID usuarioId) {
        return conexiones.registrar(usuarioId);
    }
}
