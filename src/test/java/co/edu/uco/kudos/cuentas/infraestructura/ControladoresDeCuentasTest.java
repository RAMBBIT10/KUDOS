package co.edu.uco.kudos.cuentas.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.catalogos.aplicacion.CatalogoMensajes;
import co.edu.uco.kudos.cuentas.aplicacion.IniciarSesion;
import co.edu.uco.kudos.cuentas.aplicacion.ListarSolicitudes;
import co.edu.uco.kudos.cuentas.aplicacion.ResolverSolicitud;
import co.edu.uco.kudos.cuentas.aplicacion.ResultadoSesion;
import co.edu.uco.kudos.cuentas.aplicacion.SolicitarCuenta;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.Sesiones;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Rol;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class ControladoresDeCuentasTest {

    private final Usuario pendiente = new Usuario(
            UUID.randomUUID(), "Luis", "luis@uco.net.co", "+573000000002", Rol.ESTUDIANTE, EstadoCuenta.PENDIENTE);

    private final SolicitarCuenta solicitarCuenta = mock(SolicitarCuenta.class);
    private final ListarSolicitudes listarSolicitudes = mock(ListarSolicitudes.class);
    private final ResolverSolicitud resolverSolicitud = mock(ResolverSolicitud.class);
    private final CatalogoMensajes catalogo = mock(CatalogoMensajes.class);
    private final CuentasControlador cuentas =
            new CuentasControlador(solicitarCuenta, listarSolicitudes, resolverSolicitud, catalogo);

    private final IniciarSesion iniciarSesion = mock(IniciarSesion.class);
    private final Sesiones sesiones = mock(Sesiones.class);
    private final SesionControlador sesion = new SesionControlador(iniciarSesion, sesiones);

    @Test
    void solicitarDevuelveElMensajeDelCatalogo() {
        when(catalogo.texto("SOLICITUD_ENVIADA")).thenReturn("Solicitud enviada");

        var respuesta = cuentas.solicitar(new CuentasControlador.PeticionSolicitud("Luis", "luis@uco.net.co", "+573000000002", Rol.ESTUDIANTE));

        assertThat(respuesta).containsEntry("mensaje", "Solicitud enviada");
        verify(solicitarCuenta).ejecutar(any());
    }

    @Test
    void listarDevuelveSoloLosDatosPublicos() {
        when(listarSolicitudes.ejecutar()).thenReturn(List.of(pendiente));

        var solicitudes = cuentas.listar();

        assertThat(solicitudes).hasSize(1);
        assertThat(solicitudes.get(0).correo()).isEqualTo("luis@uco.net.co");
    }

    @Test
    void aprobarYRechazarLlamanALaAplicacionYDevuelvenMensaje() {
        when(catalogo.texto("CUENTA_APROBADA")).thenReturn("Aprobada");
        when(catalogo.texto("CUENTA_RECHAZADA")).thenReturn("Rechazada");

        assertThat(cuentas.aprobar(pendiente.id())).containsEntry("mensaje", "Aprobada");
        assertThat(cuentas.rechazar(pendiente.id())).containsEntry("mensaje", "Rechazada");

        verify(resolverSolicitud).aprobar(pendiente.id());
        verify(resolverSolicitud).rechazar(pendiente.id());
    }

    @Test
    void ingresarDevuelveTokenYUsuario() {
        when(iniciarSesion.ejecutar("luis@uco.net.co", "clave")).thenReturn(new ResultadoSesion("tok", pendiente));

        var respuesta = sesion.ingresar(new SesionControlador.PeticionIngreso("luis@uco.net.co", "clave"));

        assertThat(respuesta.token()).isEqualTo("tok");
        assertThat(respuesta.usuario().nombre()).isEqualTo("Luis");
    }

    @Test
    void actualDevuelveElUsuarioDeLaSesion() {
        assertThat(sesion.actual(pendiente).correo()).isEqualTo("luis@uco.net.co");
    }

    @Test
    void salirCierraLaSesionDelToken() {
        sesion.salir(new UsernamePasswordAuthenticationToken(pendiente, "tok", List.of()));

        verify(sesiones).cerrar("tok");
    }
}
