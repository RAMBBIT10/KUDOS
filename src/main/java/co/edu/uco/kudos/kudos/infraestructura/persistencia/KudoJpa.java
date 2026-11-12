package co.edu.uco.kudos.kudos.infraestructura.persistencia;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KudoJpa extends JpaRepository<KudoEntidad, UUID> {

    List<KudoEntidad> findByDestinatarioIdOrderByCreadoEnDesc(UUID destinatarioId, Pageable pagina);

    List<KudoEntidad> findByDestinatarioIdAndCreadoEnBeforeOrderByCreadoEnDesc(
            UUID destinatarioId, Instant corte, Pageable pagina);
}
