package co.edu.uco.kudos.kudos.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.uco.kudos.kudos.aplicacion.puertos.CacheKudos;
import co.edu.uco.kudos.kudos.aplicacion.puertos.KudoRepositorio;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListarKudosTest {

    private final UUID destinatarioId = UUID.randomUUID();

    private final KudoRepositorio kudos = mock(KudoRepositorio.class);
    private final CacheKudos cache = mock(CacheKudos.class);

    private final ListarKudos casoDeUso = new ListarKudos(kudos, cache);

    private Kudo crearKudo(int segundo) {
        return new Kudo(
                UUID.randomUUID(),
                UUID.randomUUID(),
                destinatarioId,
                "Mensaje " + segundo,
                Instant.parse("2026-11-10T12:00:00Z").plusSeconds(segundo),
                List.of());
    }

    @Test
    void devuelveLoQueHayEnCacheSinConsultarLaBaseDeDatos() {
        RespuestaListaKudos guardada = new RespuestaListaKudos(List.of(crearKudo(1)), null);
        when(cache.obtener(any())).thenReturn(Optional.of(guardada));

        RespuestaListaKudos respuesta = casoDeUso.ejecutar(destinatarioId, 10, null);

        assertThat(respuesta).isSameAs(guardada);
        verify(kudos, never()).listarPorDestinatario(any(), anyInt(), any());
    }

    @Test
    void consultaLaBaseYGuardaEnCacheCuandoNoEstaba() {
        when(cache.obtener(any())).thenReturn(Optional.empty());
        when(kudos.listarPorDestinatario(destinatarioId, 11, null)).thenReturn(List.of(crearKudo(2), crearKudo(1)));

        RespuestaListaKudos respuesta = casoDeUso.ejecutar(destinatarioId, 10, null);

        assertThat(respuesta.kudos()).hasSize(2);
        assertThat(respuesta.siguienteCursor()).isNull();
        verify(cache).guardar(any(), eq(respuesta));
    }

    @Test
    void entregaElSiguienteCursorCuandoHayMasKudosQueElLimite() {
        when(cache.obtener(any())).thenReturn(Optional.empty());
        Kudo tercero = crearKudo(3);
        Kudo segundo = crearKudo(2);
        Kudo primero = crearKudo(1);
        when(kudos.listarPorDestinatario(destinatarioId, 3, null)).thenReturn(List.of(tercero, segundo, primero));

        RespuestaListaKudos respuesta = casoDeUso.ejecutar(destinatarioId, 2, null);

        assertThat(respuesta.kudos()).containsExactly(tercero, segundo);
        assertThat(respuesta.siguienteCursor()).isEqualTo(segundo.creadoEn().toString());
    }

    @Test
    void usaElCursorRecibidoComoFechaDeCorte() {
        when(cache.obtener(any())).thenReturn(Optional.empty());
        when(kudos.listarPorDestinatario(any(), anyInt(), any())).thenReturn(List.of());
        String cursor = "2026-11-10T12:00:00Z";

        casoDeUso.ejecutar(destinatarioId, 5, cursor);

        verify(kudos).listarPorDestinatario(destinatarioId, 6, Instant.parse(cursor));
    }

    @Test
    void rechazaUnCursorConFormatoInvalido() {
        when(cache.obtener(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> casoDeUso.ejecutar(destinatarioId, 5, "no-es-una-fecha"))
                .isInstanceOf(ReglaDeNegocioException.class);
    }
}
