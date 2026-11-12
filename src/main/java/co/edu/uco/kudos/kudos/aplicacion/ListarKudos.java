package co.edu.uco.kudos.kudos.aplicacion;

import co.edu.uco.kudos.kudos.aplicacion.puertos.CacheKudos;
import co.edu.uco.kudos.kudos.aplicacion.puertos.KudoRepositorio;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ListarKudos {

    private final KudoRepositorio kudos;
    private final CacheKudos cache;

    public ListarKudos(KudoRepositorio kudos, CacheKudos cache) {
        this.kudos = kudos;
        this.cache = cache;
    }

    public RespuestaListaKudos ejecutar(UUID destinatarioId, int limite, String cursor) {
        String clave = ClavesCache.listaDeKudos(destinatarioId, limite, cursor);

        Optional<RespuestaListaKudos> guardada = cache.obtener(clave);
        if (guardada.isPresent()) {
            return guardada.get();
        }

        List<Kudo> encontrados = kudos.listarPorDestinatario(destinatarioId, limite + 1, convertirCursor(cursor));

        boolean hayMas = encontrados.size() > limite;
        List<Kudo> pagina = hayMas ? encontrados.subList(0, limite) : encontrados;
        String siguienteCursor = hayMas ? pagina.get(pagina.size() - 1).creadoEn().toString() : null;

        RespuestaListaKudos respuesta = new RespuestaListaKudos(List.copyOf(pagina), siguienteCursor);
        cache.guardar(clave, respuesta);
        return respuesta;
    }

    private Instant convertirCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(cursor);
        } catch (DateTimeParseException error) {
            throw new ReglaDeNegocioException("El cursor no es valido");
        }
    }
}
