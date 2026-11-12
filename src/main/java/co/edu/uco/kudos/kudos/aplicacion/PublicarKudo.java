package co.edu.uco.kudos.kudos.aplicacion;

import co.edu.uco.kudos.kudos.aplicacion.puertos.AuditoriaRepositorio;
import co.edu.uco.kudos.kudos.aplicacion.puertos.CacheKudos;
import co.edu.uco.kudos.kudos.aplicacion.puertos.KudoRepositorio;
import co.edu.uco.kudos.kudos.aplicacion.puertos.PublicadorEventos;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.DatosNuevoKudo;
import co.edu.uco.kudos.kudos.dominio.EstadoAuditoria;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import co.edu.uco.kudos.kudos.dominio.ReglasMensaje;
import org.springframework.stereotype.Service;

@Service
public class PublicarKudo {

    private final KudoRepositorio kudos;
    private final UsuarioRepositorio usuarios;
    private final AuditoriaRepositorio auditoria;
    private final PublicadorEventos publicador;
    private final CacheKudos cache;

    public PublicarKudo(
            KudoRepositorio kudos,
            UsuarioRepositorio usuarios,
            AuditoriaRepositorio auditoria,
            PublicadorEventos publicador,
            CacheKudos cache) {
        this.kudos = kudos;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.publicador = publicador;
        this.cache = cache;
    }

    public Kudo ejecutar(PeticionPublicarKudo peticion) {
        if (!ReglasMensaje.mensajeEsValido(peticion.mensaje())) {
            throw new ReglaDeNegocioException("El mensaje no es valido");
        }

        usuarios.buscarPorId(peticion.autorId())
                .orElseThrow(() -> new NoEncontradoException("El autor no existe"));
        usuarios.buscarPorId(peticion.destinatarioId())
                .orElseThrow(() -> new NoEncontradoException("El destinatario no existe"));

        Kudo kudo = kudos.guardar(new DatosNuevoKudo(
                peticion.autorId(),
                peticion.destinatarioId(),
                peticion.mensaje().trim(),
                peticion.adjuntos()));

        auditoria.registrar(kudo.id(), "KUDO_CREADO", null, EstadoAuditoria.OK, "Kudo guardado en la base de datos");
        cache.borrarPorPrefijo(ClavesCache.prefijoDelDestinatario(kudo.destinatarioId()));
        publicador.kudoPublicado(kudo.id());

        return kudo;
    }
}
