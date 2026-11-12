package co.edu.uco.kudos.integracion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.uco.kudos.auditoria.AuditoriaEntidad;
import co.edu.uco.kudos.auditoria.AuditoriaJpa;
import co.edu.uco.kudos.kudos.dominio.EstadoAuditoria;
import co.edu.uco.kudos.notificaciones.aplicacion.ResultadoEnvio;
import co.edu.uco.kudos.notificaciones.infraestructura.NotificadorCorreo;
import co.edu.uco.kudos.notificaciones.infraestructura.NotificadorWhatsapp;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class FlujoKudoIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditoriaJpa auditoria;

    @Autowired
    private ObjectMapper convertidorJson;

    @MockBean
    private NotificadorCorreo correo;

    @MockBean
    private NotificadorWhatsapp whatsapp;

    @BeforeEach
    void prepararCanalesFalsos() {
        when(correo.canal()).thenReturn("correo");
        when(correo.enviar(any(), any())).thenReturn(ResultadoEnvio.ENVIADO);
        when(whatsapp.canal()).thenReturn("whatsapp");
        when(whatsapp.enviar(any(), any())).thenReturn(ResultadoEnvio.ENVIADO);
    }

    private UUID crearUsuario(String nombre, String telefono) throws Exception {
        String correoUnico = nombre.toLowerCase() + "-" + System.nanoTime() + "@uco.edu.co";
        String cuerpo = convertidorJson.writeValueAsString(
                Map.of("nombre", nombre, "correo", correoUnico, "telefono", telefono));

        String respuesta = mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(convertidorJson.readTree(respuesta).get("id").asText());
    }

    private MockMultipartHttpServletRequestBuilder armarPublicacion(UUID autorId, UUID destinatarioId, String mensaje) {
        return (MockMultipartHttpServletRequestBuilder) multipart("/kudos")
                .param("autorId", autorId.toString())
                .param("destinatarioId", destinatarioId.toString())
                .param("mensaje", mensaje);
    }

    private UUID publicar(UUID autorId, UUID destinatarioId, String mensaje) throws Exception {
        String respuesta = mockMvc.perform(armarPublicacion(autorId, destinatarioId, mensaje))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(convertidorJson.readTree(respuesta).get("id").asText());
    }

    private List<AuditoriaEntidad> esperarNotificaciones(UUID kudoId, int cantidadEsperada) throws InterruptedException {
        List<AuditoriaEntidad> filas = List.of();
        for (int intento = 0; intento < 50; intento++) {
            filas = auditoria.findByKudoIdAndEvento(kudoId, "NOTIFICACION");
            if (filas.size() >= cantidadEsperada) {
                return filas;
            }
            Thread.sleep(200);
        }
        return filas;
    }

    @Test
    void publicaUnKudoConAdjuntoLoGuardaNotificaPorLosTresCanalesYAudita() throws Exception {
        UUID autorId = crearUsuario("Ana", "+573000000001");
        UUID destinatarioId = crearUsuario("Luis", "+573000000002");
        MockMultipartFile adjunto = new MockMultipartFile(
                "archivos", "nota.pdf", "application/pdf", "contenido de prueba".getBytes());

        String respuesta = mockMvc.perform(
                        armarPublicacion(autorId, destinatarioId, "Gracias por ayudarme con el proyecto").file(adjunto))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.adjuntos.length()").value(1))
                .andExpect(jsonPath("$.adjuntos[0].nombre").value("nota.pdf"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID kudoId = UUID.fromString(convertidorJson.readTree(respuesta).get("id").asText());

        List<AuditoriaEntidad> notificaciones = esperarNotificaciones(kudoId, 3);
        Map<String, EstadoAuditoria> estadoPorCanal = notificaciones.stream()
                .collect(Collectors.toMap(AuditoriaEntidad::getCanal, AuditoriaEntidad::getEstado, (a, b) -> a));

        assertThat(estadoPorCanal).containsEntry("correo", EstadoAuditoria.OK);
        assertThat(estadoPorCanal).containsEntry("whatsapp", EstadoAuditoria.OK);
        assertThat(estadoPorCanal).containsEntry("push", EstadoAuditoria.OMITIDO);
        assertThat(auditoria.findByKudoIdAndEvento(kudoId, "KUDO_CREADO")).hasSize(1);
        verify(correo).enviar(any(), any());
        verify(whatsapp).enviar(any(), any());
    }

    @Test
    void listaLosKudosDelDestinatarioConPaginacionPorCursor() throws Exception {
        UUID autorId = crearUsuario("Carlos", "+573000000003");
        UUID destinatarioId = crearUsuario("Marta", "+573000000004");
        for (int numero = 1; numero <= 3; numero++) {
            publicar(autorId, destinatarioId, "Kudo numero " + numero);
            Thread.sleep(30);
        }

        String primera = mockMvc.perform(get("/kudos")
                        .param("destinatarioId", destinatarioId.toString())
                        .param("limite", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kudos.length()").value(2))
                .andExpect(jsonPath("$.kudos[0].mensaje").value("Kudo numero 3"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String cursor = convertidorJson.readTree(primera).get("siguienteCursor").asText();

        mockMvc.perform(get("/kudos")
                        .param("destinatarioId", destinatarioId.toString())
                        .param("limite", "2")
                        .param("cursor", cursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kudos.length()").value(1))
                .andExpect(jsonPath("$.kudos[0].mensaje").value("Kudo numero 1"))
                .andExpect(jsonPath("$.siguienteCursor").doesNotExist());
    }

    @Test
    void muestraElKudoNuevoAunqueLaListaYaEstuvieraEnCache() throws Exception {
        UUID autorId = crearUsuario("Pedro", "+573000000005");
        UUID destinatarioId = crearUsuario("Sofia", "+573000000006");

        mockMvc.perform(get("/kudos").param("destinatarioId", destinatarioId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kudos.length()").value(0));

        publicar(autorId, destinatarioId, "Excelente trabajo");

        mockMvc.perform(get("/kudos").param("destinatarioId", destinatarioId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kudos.length()").value(1));
    }

    @Test
    void rechazaUnMensajeVacio() throws Exception {
        UUID autorId = crearUsuario("Ines", "+573000000007");
        UUID destinatarioId = crearUsuario("Jorge", "+573000000008");

        mockMvc.perform(armarPublicacion(autorId, destinatarioId, "")).andExpect(status().isBadRequest());
    }

    @Test
    void rechazaUnArchivoDeUnTipoNoPermitido() throws Exception {
        UUID autorId = crearUsuario("Rosa", "+573000000009");
        UUID destinatarioId = crearUsuario("Tomas", "+573000000010");
        MockMultipartFile peligroso = new MockMultipartFile(
                "archivos", "virus.exe", "application/x-msdownload", "MZ".getBytes());

        mockMvc.perform(armarPublicacion(autorId, destinatarioId, "Con archivo peligroso").file(peligroso))
                .andExpect(status().isBadRequest());
    }

    @Test
    void respondeNoEncontradoSiElDestinatarioNoExiste() throws Exception {
        UUID autorId = crearUsuario("Elena", "+573000000011");

        mockMvc.perform(armarPublicacion(autorId, UUID.randomUUID(), "Hola")).andExpect(status().isNotFound());
    }

    @Test
    void respondeQueEstaVivaEnSalud() throws Exception {
        mockMvc.perform(get("/salud"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ok"));
    }
}
