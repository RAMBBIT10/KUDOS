package co.edu.uco.kudos.auditoria;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaJpa extends JpaRepository<AuditoriaEntidad, UUID> {

    List<AuditoriaEntidad> findByKudoIdAndEvento(UUID kudoId, String evento);
}
