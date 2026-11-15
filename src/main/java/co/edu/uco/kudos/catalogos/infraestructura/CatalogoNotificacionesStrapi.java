package co.edu.uco.kudos.catalogos.infraestructura;

import co.edu.uco.kudos.catalogos.aplicacion.CatalogoNotificaciones;
import co.edu.uco.kudos.catalogos.aplicacion.PlantillaNotificacion;
import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.kudos.aplicacion.NoEncontradoException;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CatalogoNotificacionesStrapi implements CatalogoNotificaciones {

    private final StrapiCliente strapi;
    private final ConfiguracionKudos.ColeccionNotificaciones coleccion;
    private final CacheTemporal<PlantillaNotificacion> cache;

    public CatalogoNotificacionesStrapi(StrapiCliente strapi, ConfiguracionKudos configuracion) {
        this.strapi = strapi;
        this.coleccion = configuracion.strapi().notificaciones();
        this.cache = new CacheTemporal<>(configuracion.cache().segundosDeVida());
    }

    @Override
    public PlantillaNotificacion plantilla(String canal) {
        return cache.obtener(canal, () -> buscar(canal))
                .orElseThrow(() -> new NoEncontradoException("PLANTILLA_NO_EXISTE"));
    }

    private Optional<PlantillaNotificacion> buscar(String canal) {
        return strapi.buscarPor(coleccion.ruta(), coleccion.campoCanal(), canal)
                .map(fila -> new PlantillaNotificacion(
                        fila.path(coleccion.campoAsunto()).asText(""),
                        fila.path(coleccion.campoCuerpo()).asText("")));
    }
}
