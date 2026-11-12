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
        Cors cors) {

    public record Archivos(String carpeta, long tamanoMaximoEnBytes, List<String> tiposPermitidos, int cantidadMaxima) {
    }

    public record Cache(int segundosDeVida) {
    }

    public record Correo(String remitente) {
    }

    public record Whatsapp(String urlBase, String cuentaSid, String token, String numeroOrigen) {
    }

    public record Cola(String nombre, int intentosMaximos, int esperaBaseEnSegundos, List<String> canales) {
    }

    public record Cors(List<String> origenesPermitidos) {
    }
}
