package co.edu.uco.kudos.kudos.infraestructura.web;

import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuariosControlador {

    public record UsuarioPublico(UUID id, String nombre) {
    }

    private final UsuarioRepositorio usuarios;

    public UsuariosControlador(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    public List<UsuarioPublico> listar() {
        return usuarios.listarPorEstado(EstadoCuenta.ACTIVO).stream()
                .map(usuario -> new UsuarioPublico(usuario.id(), usuario.nombre()))
                .toList();
    }
}
