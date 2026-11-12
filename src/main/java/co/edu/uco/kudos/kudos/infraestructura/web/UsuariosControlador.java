package co.edu.uco.kudos.kudos.infraestructura.web;

import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuariosControlador {

    public record PeticionCrearUsuario(
            @NotBlank String nombre,
            @NotBlank @Email String correo,
            @NotBlank String telefono) {
    }

    public record UsuarioPublico(UUID id, String nombre) {
    }

    private final UsuarioRepositorio usuarios;

    public UsuariosControlador(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    public List<UsuarioPublico> listar() {
        return usuarios.listar().stream()
                .map(usuario -> new UsuarioPublico(usuario.id(), usuario.nombre()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crear(@Valid @RequestBody PeticionCrearUsuario peticion) {
        return usuarios.guardar(peticion.nombre(), peticion.correo(), peticion.telefono());
    }
}
