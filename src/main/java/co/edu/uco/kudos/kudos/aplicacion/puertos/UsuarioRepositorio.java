package co.edu.uco.kudos.kudos.aplicacion.puertos;

import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Rol;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositorio {

    Usuario guardar(String nombre, String correo, String telefono, Rol rol, EstadoCuenta estado, String claveCifrada);

    Usuario actualizarCuenta(UUID id, EstadoCuenta estado, String claveCifrada);

    Optional<Usuario> buscarPorId(UUID id);

    Optional<Usuario> buscarPorCorreo(String correo);

    Optional<String> claveCifradaDe(UUID id);

    List<Usuario> listarPorEstado(EstadoCuenta estado);
}
