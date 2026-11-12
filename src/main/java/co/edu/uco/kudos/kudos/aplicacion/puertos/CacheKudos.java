package co.edu.uco.kudos.kudos.aplicacion.puertos;

import co.edu.uco.kudos.kudos.aplicacion.RespuestaListaKudos;
import java.util.Optional;

public interface CacheKudos {

    Optional<RespuestaListaKudos> obtener(String clave);

    void guardar(String clave, RespuestaListaKudos valor);

    void borrarPorPrefijo(String prefijo);
}
