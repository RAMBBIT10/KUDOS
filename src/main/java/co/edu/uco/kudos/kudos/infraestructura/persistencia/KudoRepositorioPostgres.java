package co.edu.uco.kudos.kudos.infraestructura.persistencia;

import co.edu.uco.kudos.kudos.aplicacion.puertos.KudoRepositorio;
import co.edu.uco.kudos.kudos.dominio.Adjunto;
import co.edu.uco.kudos.kudos.dominio.DatosNuevoKudo;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class KudoRepositorioPostgres implements KudoRepositorio {

    private final KudoJpa tabla;

    public KudoRepositorioPostgres(KudoJpa tabla) {
        this.tabla = tabla;
    }

    @Override
    public Kudo guardar(DatosNuevoKudo datos) {
        KudoEntidad entidad = new KudoEntidad();
        entidad.setAutorId(datos.autorId());
        entidad.setDestinatarioId(datos.destinatarioId());
        entidad.setMensaje(datos.mensaje());
        entidad.setCreadoEn(Instant.now().truncatedTo(ChronoUnit.MICROS));

        for (Adjunto adjunto : datos.adjuntos()) {
            AdjuntoEntidad adjuntoEntidad = new AdjuntoEntidad();
            adjuntoEntidad.setNombre(adjunto.nombre());
            adjuntoEntidad.setTipo(adjunto.tipo());
            adjuntoEntidad.setTamano(adjunto.tamano());
            adjuntoEntidad.setUrl(adjunto.url());
            adjuntoEntidad.setKudo(entidad);
            entidad.getAdjuntos().add(adjuntoEntidad);
        }

        return convertir(tabla.save(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Kudo> buscarPorId(UUID id) {
        return tabla.findById(id).map(this::convertir);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Kudo> listarPorDestinatario(UUID destinatarioId, int limite, Instant cursor) {
        PageRequest pagina = PageRequest.of(0, limite);
        List<KudoEntidad> encontrados = cursor == null
                ? tabla.findByDestinatarioIdOrderByCreadoEnDesc(destinatarioId, pagina)
                : tabla.findByDestinatarioIdAndCreadoEnBeforeOrderByCreadoEnDesc(destinatarioId, cursor, pagina);
        return encontrados.stream().map(this::convertir).toList();
    }

    private Kudo convertir(KudoEntidad entidad) {
        List<Adjunto> adjuntos = entidad.getAdjuntos().stream()
                .map(adjunto -> new Adjunto(adjunto.getNombre(), adjunto.getTipo(), adjunto.getTamano(), adjunto.getUrl()))
                .toList();
        return new Kudo(
                entidad.getId(),
                entidad.getAutorId(),
                entidad.getDestinatarioId(),
                entidad.getMensaje(),
                entidad.getCreadoEn(),
                adjuntos);
    }
}
