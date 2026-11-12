package co.edu.uco.kudos.kudos.infraestructura.persistencia;

import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepositorioPostgres implements UsuarioRepositorio {

    private final UsuarioJpa tabla;

    public UsuarioRepositorioPostgres(UsuarioJpa tabla) {
        this.tabla = tabla;
    }

    @Override
    public Usuario guardar(String nombre, String correo, String telefono) {
        UsuarioEntidad entidad = new UsuarioEntidad();
        entidad.setNombre(nombre);
        entidad.setCorreo(correo);
        entidad.setTelefono(telefono);
        return convertir(tabla.saveAndFlush(entidad));
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return tabla.findById(id).map(this::convertir);
    }

    @Override
    public List<Usuario> listar() {
        return tabla.findAll().stream().map(this::convertir).toList();
    }

    private Usuario convertir(UsuarioEntidad entidad) {
        return new Usuario(entidad.getId(), entidad.getNombre(), entidad.getCorreo(), entidad.getTelefono());
    }
}
