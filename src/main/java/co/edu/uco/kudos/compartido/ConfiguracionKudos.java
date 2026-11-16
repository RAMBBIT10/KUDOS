package co.edu.uco.kudos.compartido;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kudos")
public record ConfiguracionKudos(
        Archivos archivos,
        Cache cache,
        Correo correo,
        Whatsapp whatsapp,
        Cola cola,
        Cors cors,
        Canales canales,
        Strapi strapi,
        Cuentas cuentas) {

    public record Archivos(String carpeta, long tamanoMaximoEnBytes, List<String> tiposPermitidos, int cantidadMaxima) {
    }

    public record Cache(int segundosDeVida) {
    }

    public record Correo(String remitente) {
    }

    public record Whatsapp(
            String urlBase,
            String rutaEnvio,
            String prefijo,
            String cuentaSid,
            String token,
            String numeroOrigen) {
    }

    public record Cola(String nombre, int intentosMaximos, int esperaBaseEnSegundos, List<String> canales) {
    }

    public record Cors(List<String> origenesPermitidos) {
    }

    public record Canales(String correo, String whatsapp, String push) {
    }

    public record Strapi(
            String urlBase,
            String token,
            ColeccionMensajes mensajes,
            ColeccionNotificaciones notificaciones) {
    }

    public record ColeccionMensajes(String ruta, String campoCodigo, String campoTexto) {
    }

    public record ColeccionNotificaciones(String ruta, String campoCanal, String campoAsunto, String campoCuerpo) {
    }

    public record Cuentas(
            String prefijoTelefono,
            int largoMinimoClave,
            int longitudClaveTemporal,
            int segundosDeSesion,
            String prefijoSesion,
            String administradorNombre,
            String administradorCorreo,
            String administradorTelefono,
            String administradorClave,
            String plantillaSolicitud,
            String plantillaAprobada,
            String plantillaRechazada,
            String plantillaAprobadaWhatsapp) {
    }
}
