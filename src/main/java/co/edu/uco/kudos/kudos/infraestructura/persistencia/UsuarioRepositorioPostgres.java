package co.edu.uco.kudos.kudos.infraestructura.persistencia;

import co.edu.uco.kudos.kudos.aplicacion.NoEncontradoException;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Rol;
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
    public Usuario guardar(String nombre, String correo, String telefono, Rol rol, EstadoCuenta estado, String claveCifrada) {
        UsuarioEntidad entidad = new UsuarioEntidad();
        entidad.setNombre(nombre);
        entidad.setCorreo(correo);
        entidad.setTelefono(telefono);
        entidad.setRol(rol);
        entidad.setEstado(estado);
        entidad.setClaveCifrada(claveCifrada);
        return convertir(tabla.saveAndFlush(entidad));
    }

    @Override
    public Usuario actualizarCuenta(UUID id, EstadoCuenta estado, String claveCifrada) {
        UsuarioEntidad entidad = tabla.findById(id)
                .orElseThrow(() -> new NoEncontradoException("SOLICITUD_NO_EXISTE"));
        entidad.setEstado(estado);
        entidad.setClaveCifrada(claveCifrada);
        return convertir(tabla.saveAndFlush(entidad));
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return tabla.findById(id).map(this::convertir);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return tabla.findByCorreo(correo).map(this::convertir);
    }

    @Override
    public Optional<String> claveCifradaDe(UUID id) {
        return tabla.findById(id).map(UsuarioEntidad::getClaveCifrada);
    }

    @Override
    public List<Usuario> listarPorEstado(EstadoCuenta estado) {
        return tabla.findByEstadoOrderByNombreAsc(estado).stream().map(this::convertir).toList();
    }

    private Usuario convertir(UsuarioEntidad entidad) {
        Rol rol = entidad.getRol() == null ? Rol.ESTUDIANTE : entidad.getRol();
        EstadoCuenta estado = entidad.getEstado() == null ? EstadoCuenta.RECHAZADO : entidad.getEstado();
        return new Usuario(entidad.getId(), entidad.getNombre(), entidad.getCorreo(), entidad.getTelefono(), rol, estado);
    }
}
