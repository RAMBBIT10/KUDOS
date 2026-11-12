package co.edu.uco.kudos.kudos.infraestructura.persistencia;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpa extends JpaRepository<UsuarioEntidad, UUID> {
}
