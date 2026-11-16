package co.edu.uco.kudos.kudos.infraestructura.persistencia;

import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpa extends JpaRepository<UsuarioEntidad, UUID> {

    Optional<UsuarioEntidad> findByCorreo(String correo);

    List<UsuarioEntidad> findByEstadoOrderByNombreAsc(EstadoCuenta estado);
}
