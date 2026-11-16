package co.edu.uco.kudos.notificaciones.infraestructura;

import co.edu.uco.kudos.kudos.dominio.Usuario;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class PushControlador {

    private final ConexionesPush conexiones;

    public PushControlador(ConexionesPush conexiones) {
        this.conexiones = conexiones;
    }

    @GetMapping(path = "/push", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter conectar(@AuthenticationPrincipal Usuario usuario) {
        return conexiones.registrar(usuario.id());
    }
}
