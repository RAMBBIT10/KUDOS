package co.edu.uco.kudos.catalogos.infraestructura;

import co.edu.uco.kudos.catalogos.aplicacion.CatalogoMensajes;
import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class CatalogoMensajesStrapi implements CatalogoMensajes {

    private final StrapiCliente strapi;
    private final ConfiguracionKudos.ColeccionMensajes coleccion;
    private final CacheTemporal<String> cache;

    public CatalogoMensajesStrapi(StrapiCliente strapi, ConfiguracionKudos configuracion) {
        this.strapi = strapi;
        this.coleccion = configuracion.strapi().mensajes();
        this.cache = new CacheTemporal<>(configuracion.cache().segundosDeVida());
    }

    @Override
    public String texto(String codigo) {
        try {
            return cache.obtener(codigo, () -> buscar(codigo)).orElse(codigo);
        } catch (RestClientException error) {
            return codigo;
        }
    }

    private Optional<String> buscar(String codigo) {
        return strapi.buscarPor(coleccion.ruta(), coleccion.campoCodigo(), codigo)
                .map(fila -> fila.path(coleccion.campoTexto()).asText());
    }
}
