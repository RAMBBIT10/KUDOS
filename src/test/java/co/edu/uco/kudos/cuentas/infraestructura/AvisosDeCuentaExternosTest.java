package co.edu.uco.kudos.cuentas.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.catalogos.aplicacion.CatalogoNotificaciones;
import co.edu.uco.kudos.catalogos.aplicacion.PlantillaNotificacion;
import co.edu.uco.kudos.compartido.ConfiguracionDePrueba;
import co.edu.uco.kudos.kudos.aplicacion.ServicioExternoException;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.dominio.Rol;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import co.edu.uco.kudos.notificaciones.infraestructura.ClienteWhatsapp;
import co.edu.uco.kudos.notificaciones.infraestructura.EnviadorCorreo;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AvisosDeCuentaExternosTest {

    private final Usuario usuario = new Usuario(
            UUID.randomUUID(), "Luis Perez", "luis.perez1234@uco.net.co", "+573000000002", Rol.ESTUDIANTE, EstadoCuenta.PENDIENTE);

    private final EnviadorCorreo correo = mock(EnviadorCorreo.class);
    private final ClienteWhatsapp whatsapp = mock(ClienteWhatsapp.class);
    private final CatalogoNotificaciones catalogo = mock(CatalogoNotificaciones.class);
    private final AvisosDeCuentaExternos avisos =
            new AvisosDeCuentaExternos(correo, whatsapp, catalogo, ConfiguracionDePrueba.sinStrapi());

    @Test
    void laSolicitudSeAvisaAlCorreoDelAdministrador() {
        when(catalogo.plantilla("cuenta-solicitud"))
                .thenReturn(new PlantillaNotificacion("Nueva solicitud", "{nombre} {correo} {rol}"));

        avisos.solicitudRecibida(usuario);

        verify(correo).enviar("admin.prueba0000@uco.net.co", "Nueva solicitud", "Luis Perez luis.perez1234@uco.net.co ESTUDIANTE");
    }

    @Test
    void siFallaElAvisoDeLaSolicitudNoSeLanzaError() {
        when(catalogo.plantilla(anyString())).thenThrow(new RuntimeException("sin catalogo"));

        assertThatCode(() -> avisos.solicitudRecibida(usuario)).doesNotThrowAnyException();
    }

    @Test
    void laAprobacionAvisaPorCorreoYPorWhatsappConLaClave() {
        when(catalogo.plantilla("cuenta-aprobada")).thenReturn(new PlantillaNotificacion("Aprobada", "Clave {clave}"));
        when(catalogo.plantilla("cuenta-aprobada-whatsapp")).thenReturn(new PlantillaNotificacion("", "WA {clave}"));

        avisos.cuentaAprobada(usuario, "Temp123");

        verify(correo).enviar("luis.perez1234@uco.net.co", "Aprobada", "Clave Temp123");
        verify(whatsapp).enviar("+573000000002", "WA Temp123");
    }

    @Test
    void siFallanElCorreoYWhatsappSeLanzaServicioExterno() {
        when(catalogo.plantilla("cuenta-aprobada")).thenReturn(new PlantillaNotificacion("Aprobada", "Clave {clave}"));
        when(catalogo.plantilla("cuenta-aprobada-whatsapp")).thenReturn(new PlantillaNotificacion("", "WA {clave}"));
        doThrow(new RuntimeException("smtp caido")).when(correo).enviar(anyString(), anyString(), anyString());
        doThrow(new RuntimeException("twilio caido")).when(whatsapp).enviar(anyString(), anyString());

        assertThatThrownBy(() -> avisos.cuentaAprobada(usuario, "Temp123"))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessage("AVISO_NO_ENVIADO");
    }

    @Test
    void siFallaSoloElCorreoElWhatsappIgualSale() {
        when(catalogo.plantilla("cuenta-aprobada")).thenReturn(new PlantillaNotificacion("Aprobada", "Clave {clave}"));
        when(catalogo.plantilla("cuenta-aprobada-whatsapp")).thenReturn(new PlantillaNotificacion("", "WA {clave}"));
        doThrow(new RuntimeException("smtp caido")).when(correo).enviar(anyString(), anyString(), anyString());

        assertThatCode(() -> avisos.cuentaAprobada(usuario, "Temp123")).doesNotThrowAnyException();
        verify(whatsapp).enviar("+573000000002", "WA Temp123");
    }

    @Test
    void siFallaWhatsappLaAprobacionSigueSinError() {
        when(catalogo.plantilla("cuenta-aprobada")).thenReturn(new PlantillaNotificacion("Aprobada", "Clave {clave}"));
        when(catalogo.plantilla("cuenta-aprobada-whatsapp")).thenReturn(new PlantillaNotificacion("", "WA {clave}"));
        doThrow(new RuntimeException("twilio caido")).when(whatsapp).enviar(anyString(), anyString());

        assertThatCode(() -> avisos.cuentaAprobada(usuario, "Temp123")).doesNotThrowAnyException();
        verify(correo).enviar("luis.perez1234@uco.net.co", "Aprobada", "Clave Temp123");
    }

    @Test
    void elRechazoSeAvisaPorCorreo() {
        when(catalogo.plantilla("cuenta-rechazada")).thenReturn(new PlantillaNotificacion("Rechazada", "Hola {nombre}"));

        avisos.cuentaRechazada(usuario);

        verify(correo).enviar("luis.perez1234@uco.net.co", "Rechazada", "Hola Luis Perez");
    }

    @Test
    void siFallaElCorreoDeRechazoSeLanzaServicioExterno() {
        when(catalogo.plantilla(anyString())).thenThrow(new RuntimeException("sin catalogo"));

        assertThatThrownBy(() -> avisos.cuentaRechazada(usuario)).isInstanceOf(ServicioExternoException.class);
        assertThat(usuario.estaActivo()).isFalse();
    }
}
