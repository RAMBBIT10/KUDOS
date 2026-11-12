package co.edu.uco.kudos.kudos.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.kudos.aplicacion.puertos.AuditoriaRepositorio;
import co.edu.uco.kudos.kudos.aplicacion.puertos.CacheKudos;
import co.edu.uco.kudos.kudos.aplicacion.puertos.KudoRepositorio;
import co.edu.uco.kudos.kudos.aplicacion.puertos.PublicadorEventos;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;
import co.edu.uco.kudos.kudos.dominio.EstadoAuditoria;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import co.edu.uco.kudos.kudos.dominio.Usuario;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PublicarKudoTest {

    private final UUID autorId = UUID.randomUUID();
    private final UUID destinatarioId = UUID.randomUUID();
    private final UUID kudoId = UUID.randomUUID();

    private final KudoRepositorio kudos = mock(KudoRepositorio.class);
    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final AuditoriaRepositorio auditoria = mock(AuditoriaRepositorio.class);
    private final PublicadorEventos publicador = mock(PublicadorEventos.class);
    private final CacheKudos cache = mock(CacheKudos.class);

    private final PublicarKudo casoDeUso = new PublicarKudo(kudos, usuarios, auditoria, publicador, cache);

    private void existenAutorYDestinatario() {
        when(usuarios.buscarPorId(autorId))
                .thenReturn(Optional.of(new Usuario(autorId, "Ana", "ana@uco.edu.co", "+573000000001")));
        when(usuarios.buscarPorId(destinatarioId))
                .thenReturn(Optional.of(new Usuario(destinatarioId, "Luis", "luis@uco.edu.co", "+573000000002")));
    }

    @Test
    void guardaElKudoRegistraAuditoriaLimpiaLaCacheYPublicaElEvento() {
        existenAutorYDestinatario();
        Kudo guardado = new Kudo(kudoId, autorId, destinatarioId, "Gracias por tu ayuda", Instant.now(), List.of());
        when(kudos.guardar(any())).thenReturn(guardado);

        Kudo resultado = casoDeUso.ejecutar(
                new PeticionPublicarKudo(autorId, destinatarioId, "Gracias por tu ayuda", List.of()));

        assertThat(resultado.id()).isEqualTo(kudoId);
        verify(auditoria).registrar(eq(kudoId), eq("KUDO_CREADO"), isNull(), eq(EstadoAuditoria.OK), anyString());
        verify(cache).borrarPorPrefijo("kudos:" + destinatarioId);
        verify(publicador).kudoPublicado(kudoId);
    }

    @Test
    void rechazaUnMensajeVacioYNoGuardaNada() {
        PeticionPublicarKudo peticion = new PeticionPublicarKudo(autorId, destinatarioId, "   ", List.of());

        assertThatThrownBy(() -> casoDeUso.ejecutar(peticion)).isInstanceOf(ReglaDeNegocioException.class);

        verify(kudos, never()).guardar(any());
    }

    @Test
    void rechazaUnDestinatarioQueNoExiste() {
        when(usuarios.buscarPorId(autorId))
                .thenReturn(Optional.of(new Usuario(autorId, "Ana", "ana@uco.edu.co", "+573000000001")));
        when(usuarios.buscarPorId(destinatarioId)).thenReturn(Optional.empty());
        PeticionPublicarKudo peticion = new PeticionPublicarKudo(autorId, destinatarioId, "Hola", List.of());

        assertThatThrownBy(() -> casoDeUso.ejecutar(peticion)).isInstanceOf(NoEncontradoException.class);

        verify(publicador, never()).kudoPublicado(any());
    }

    @Test
    void rechazaUnAutorQueNoExiste() {
        when(usuarios.buscarPorId(autorId)).thenReturn(Optional.empty());
        PeticionPublicarKudo peticion = new PeticionPublicarKudo(autorId, destinatarioId, "Hola", List.of());

        assertThatThrownBy(() -> casoDeUso.ejecutar(peticion)).isInstanceOf(NoEncontradoException.class);

        verify(kudos, never()).guardar(any());
    }
}
