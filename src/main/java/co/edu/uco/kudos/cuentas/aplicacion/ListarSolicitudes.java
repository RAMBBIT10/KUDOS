package co.edu.uco.kudos.cuentas.aplicacion;

import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListarSolicitudes {

    private final UsuarioRepositorio usuarios;

    public ListarSolicitudes(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    public List<Usuario> ejecutar() {
        return usuarios.listarPorEstado(EstadoCuenta.PENDIENTE);
    }
}
