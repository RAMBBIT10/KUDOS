package co.edu.uco.kudos.notificaciones.aplicacion;

import co.edu.uco.kudos.kudos.aplicacion.NoEncontradoException;
import co.edu.uco.kudos.kudos.aplicacion.puertos.AuditoriaRepositorio;
import co.edu.uco.kudos.kudos.aplicacion.puertos.KudoRepositorio;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoAuditoria;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EnviarNotificacion {

    private static final String EVENTO = "NOTIFICACION";

    private final List<Notificador> notificadores;
    private final KudoRepositorio kudos;
    private final UsuarioRepositorio usuarios;
    private final AuditoriaRepositorio auditoria;

    public EnviarNotificacion(
            List<Notificador> notificadores,
            KudoRepositorio kudos,
            UsuarioRepositorio usuarios,
            AuditoriaRepositorio auditoria) {
        this.notificadores = notificadores;
        this.kudos = kudos;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
    }

    public void ejecutar(UUID kudoId, String canal) {
        Notificador notificador = notificadores.stream()
                .filter(candidato -> canal.equals(candidato.canal()))
                .findFirst()
                .orElseThrow(() -> new NoEncontradoException("No existe el canal " + canal));

        Kudo kudo = kudos.buscarPorId(kudoId)
                .orElseThrow(() -> new NoEncontradoException("El kudo no existe"));
        Usuario destinatario = usuarios.buscarPorId(kudo.destinatarioId())
                .orElseThrow(() -> new NoEncontradoException("El destinatario no existe"));

        try {
            ResultadoEnvio resultado = notificador.enviar(destinatario, kudo);
            if (resultado == ResultadoEnvio.ENVIADO) {
                auditoria.registrar(kudo.id(), EVENTO, canal, EstadoAuditoria.OK, "Notificacion enviada");
            } else {
                auditoria.registrar(kudo.id(), EVENTO, canal, EstadoAuditoria.OMITIDO, "El destinatario no esta conectado");
            }
        } catch (RuntimeException error) {
            auditoria.registrar(kudo.id(), EVENTO, canal, EstadoAuditoria.ERROR, error.getMessage());
            throw error;
        }
    }
}
