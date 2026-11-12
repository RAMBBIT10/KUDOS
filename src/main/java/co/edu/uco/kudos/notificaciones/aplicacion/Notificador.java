package co.edu.uco.kudos.notificaciones.aplicacion;

import co.edu.uco.kudos.kudos.dominio.Kudo;
import co.edu.uco.kudos.kudos.dominio.Usuario;

public interface Notificador {

    String canal();

    ResultadoEnvio enviar(Usuario destinatario, Kudo kudo);
}
