package co.edu.uco.kudos.cuentas.infraestructura;

import co.edu.uco.kudos.catalogos.aplicacion.CatalogoMensajes;
import co.edu.uco.kudos.cuentas.aplicacion.ListarSolicitudes;
import co.edu.uco.kudos.cuentas.aplicacion.PeticionSolicitudCuenta;
import co.edu.uco.kudos.cuentas.aplicacion.ResolverSolicitud;
import co.edu.uco.kudos.cuentas.aplicacion.SolicitarCuenta;
import co.edu.uco.kudos.kudos.dominio.Rol;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cuentas/solicitudes")
public class CuentasControlador {

    public record PeticionSolicitud(String nombre, String correo, String telefono, Rol rol) {
    }

    public record SolicitudPublica(UUID id, String nombre, String correo, String telefono, Rol rol) {
    }

    private final SolicitarCuenta solicitarCuenta;
    private final ListarSolicitudes listarSolicitudes;
    private final ResolverSolicitud resolverSolicitud;
    private final CatalogoMensajes catalogo;

    public CuentasControlador(
            SolicitarCuenta solicitarCuenta,
            ListarSolicitudes listarSolicitudes,
            ResolverSolicitud resolverSolicitud,
            CatalogoMensajes catalogo) {
        this.solicitarCuenta = solicitarCuenta;
        this.listarSolicitudes = listarSolicitudes;
        this.resolverSolicitud = resolverSolicitud;
        this.catalogo = catalogo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> solicitar(@RequestBody PeticionSolicitud peticion) {
        solicitarCuenta.ejecutar(new PeticionSolicitudCuenta(
                peticion.nombre(), peticion.correo(), peticion.telefono(), peticion.rol()));
        return Map.of("mensaje", catalogo.texto("SOLICITUD_ENVIADA"));
    }

    @GetMapping
    public List<SolicitudPublica> listar() {
        return listarSolicitudes.ejecutar().stream()
                .map(usuario -> new SolicitudPublica(
                        usuario.id(), usuario.nombre(), usuario.correo(), usuario.telefono(), usuario.rol()))
                .toList();
    }

    @PostMapping("/{id}/aprobar")
    public Map<String, String> aprobar(@PathVariable UUID id) {
        resolverSolicitud.aprobar(id);
        return Map.of("mensaje", catalogo.texto("CUENTA_APROBADA"));
    }

    @PostMapping("/{id}/rechazar")
    public Map<String, String> rechazar(@PathVariable UUID id) {
        resolverSolicitud.rechazar(id);
        return Map.of("mensaje", catalogo.texto("CUENTA_RECHAZADA"));
    }
}
