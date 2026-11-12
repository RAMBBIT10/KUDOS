package co.edu.uco.kudos.notificaciones.infraestructura;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class ConexionesPush {

    private static final long TIEMPO_MAXIMO_EN_MILISEGUNDOS = 30L * 60 * 1000;

    private final Map<UUID, List<SseEmitter>> conectados = new ConcurrentHashMap<>();

    public SseEmitter registrar(UUID usuarioId) {
        SseEmitter canal = new SseEmitter(TIEMPO_MAXIMO_EN_MILISEGUNDOS);
        conectados.computeIfAbsent(usuarioId, id -> new CopyOnWriteArrayList<>()).add(canal);
        canal.onCompletion(() -> retirar(usuarioId, canal));
        canal.onTimeout(() -> retirar(usuarioId, canal));
        canal.onError(error -> retirar(usuarioId, canal));
        return canal;
    }

    public void retirar(UUID usuarioId, SseEmitter canal) {
        conectados.computeIfPresent(usuarioId, (id, lista) -> {
            lista.remove(canal);
            return lista.isEmpty() ? null : lista;
        });
    }

    public boolean estaConectado(UUID usuarioId) {
        return conectados.containsKey(usuarioId);
    }

    public void enviarAlUsuario(UUID usuarioId, String evento, Object datos) {
        for (SseEmitter canal : conectados.getOrDefault(usuarioId, List.of())) {
            try {
                canal.send(SseEmitter.event().name(evento).data(datos));
            } catch (IOException | IllegalStateException error) {
                retirar(usuarioId, canal);
            }
        }
    }
}
