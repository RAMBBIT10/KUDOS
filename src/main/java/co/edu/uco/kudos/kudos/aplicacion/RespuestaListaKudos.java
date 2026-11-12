package co.edu.uco.kudos.kudos.aplicacion;

import co.edu.uco.kudos.kudos.dominio.Kudo;
import java.util.List;

public record RespuestaListaKudos(List<Kudo> kudos, String siguienteCursor) {
}
