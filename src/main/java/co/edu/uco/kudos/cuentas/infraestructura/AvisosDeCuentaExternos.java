package co.edu.uco.kudos.cuentas.infraestructura;

import co.edu.uco.kudos.catalogos.aplicacion.CatalogoNotificaciones;
import co.edu.uco.kudos.catalogos.aplicacion.PlantillaNotificacion;
import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.AvisosDeCuenta;
import co.edu.uco.kudos.kudos.aplicacion.ServicioExternoException;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import co.edu.uco.kudos.notificaciones.infraestructura.ClienteWhatsapp;
import co.edu.uco.kudos.notificaciones.infraestructura.EnviadorCorreo;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AvisosDeCuentaExternos implements AvisosDeCuenta {

    private static final Logger registro = LoggerFactory.getLogger(AvisosDeCuentaExternos.class);

    private final EnviadorCorreo correo;
    private final ClienteWhatsapp whatsapp;
    private final CatalogoNotificaciones catalogo;
    private final ConfiguracionKudos.Cuentas datos;

    public AvisosDeCuentaExternos(
            EnviadorCorreo correo,
            ClienteWhatsapp whatsapp,
            CatalogoNotificaciones catalogo,
            ConfiguracionKudos configuracion) {
        this.correo = correo;
        this.whatsapp = whatsapp;
        this.catalogo = catalogo;
        this.datos = configuracion.cuentas();
    }

    @Override
    public void solicitudRecibida(Usuario solicitante) {
        try {
            PlantillaNotificacion plantilla = catalogo.plantilla(datos.plantillaSolicitud());
            String texto = plantilla.rellenar(Map.of(
                    "nombre", solicitante.nombre(),
                    "correo", solicitante.correo(),
                    "rol", solicitante.rol().name()));
            correo.enviar(datos.administradorCorreo(), plantilla.asunto(), texto);
        } catch (RuntimeException error) {
            registro.warn("No se pudo avisar al administrador de la solicitud de {}", solicitante.correo());
        }
    }

    @Override
    public void cuentaAprobada(Usuario usuario, String claveTemporal) {
        Map<String, String> valores = Map.of(
                "nombre", usuario.nombre(),
                "correo", usuario.correo(),
                "clave", claveTemporal);
        enviarCorreo(usuario, datos.plantillaAprobada(), valores);
        avisarPorWhatsapp(usuario, valores);
    }

    @Override
    public void cuentaRechazada(Usuario usuario) {
        enviarCorreo(usuario, datos.plantillaRechazada(), Map.of("nombre", usuario.nombre(), "correo", usuario.correo()));
    }

    private void enviarCorreo(Usuario usuario, String nombrePlantilla, Map<String, String> valores) {
        try {
            PlantillaNotificacion plantilla = catalogo.plantilla(nombrePlantilla);
            correo.enviar(usuario.correo(), plantilla.asunto(), plantilla.rellenar(valores));
        } catch (RuntimeException error) {
            throw new ServicioExternoException("AVISO_NO_ENVIADO", error);
        }
    }

    private void avisarPorWhatsapp(Usuario usuario, Map<String, String> valores) {
        try {
            PlantillaNotificacion plantilla = catalogo.plantilla(datos.plantillaAprobadaWhatsapp());
            whatsapp.enviar(usuario.telefono(), plantilla.rellenar(valores));
        } catch (RuntimeException error) {
            registro.warn("No se pudo avisar por WhatsApp a {}", usuario.correo());
        }
    }
}
