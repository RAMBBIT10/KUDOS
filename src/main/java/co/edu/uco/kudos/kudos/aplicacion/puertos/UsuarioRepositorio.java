package co.edu.uco.kudos.kudos.aplicacion.puertos;

import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositorio {

    Usuario guardar(String nombre, String correo, String telefono);

    Optional<Usuario> buscarPorId(UUID id);

    List<Usuario> listar();
}
