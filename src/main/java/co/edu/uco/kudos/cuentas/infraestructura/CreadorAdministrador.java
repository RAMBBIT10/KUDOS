package co.edu.uco.kudos.cuentas.infraestructura;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.CifradorClaves;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Rol;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CreadorAdministrador implements ApplicationRunner {

    private final UsuarioRepositorio usuarios;
    private final CifradorClaves cifrador;
    private final ConfiguracionKudos.Cuentas datos;

    public CreadorAdministrador(UsuarioRepositorio usuarios, CifradorClaves cifrador, ConfiguracionKudos configuracion) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.datos = configuracion.cuentas();
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        String correo = datos.administradorCorreo().trim().toLowerCase();
        if (usuarios.buscarPorCorreo(correo).isPresent()) {
            return;
        }
        usuarios.guardar(
                datos.administradorNombre(),
                correo,
                datos.administradorTelefono(),
                Rol.ADMINISTRADOR,
                EstadoCuenta.ACTIVO,
                cifrador.cifrar(datos.administradorClave()));
    }
}
