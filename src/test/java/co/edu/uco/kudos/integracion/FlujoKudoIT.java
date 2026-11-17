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
import co.edu.uco.kudos.catalogos.aplicacion.CatalogoMensajes;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.AvisosDeCuenta;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.CifradorClaves;
import co.edu.uco.kudos.kudos.dominio.EstadoCuenta;
import co.edu.uco.kudos.kudos.infraestructura.persistencia.UsuarioEntidad;
import co.edu.uco.kudos.kudos.infraestructura.persistencia.UsuarioJpa;
import co.edu.uco.kudos.kudos.dominio.EstadoAuditoria;
import co.edu.uco.kudos.notificaciones.aplicacion.ResultadoEnvio;
import co.edu.uco.kudos.notificaciones.infraestructura.NotificadorCorreo;
import co.edu.uco.kudos.notificaciones.infraestructura.NotificadorWhatsapp;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@SpringBootTest(properties = {
        "kudos.cuentas.administrador-nombre=Administrador de prueba",
        "kudos.cuentas.administrador-correo=admin.prueba0000@uco.net.co",
        "kudos.cuentas.administrador-clave=clave_admin_de_prueba"
})
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

    @MockBean
    private CatalogoMensajes catalogoMensajes;

    @MockBean
    private AvisosDeCuenta avisos;

    @Autowired
    private UsuarioJpa usuarios;

    @Autowired
    private CifradorClaves cifrador;

    @Value("${kudos.cuentas.administrador-correo}")
    private String correoAdmin;

    @Value("${kudos.cuentas.administrador-clave}")
    private String claveAdmin;

    @BeforeEach
    void prepararCanalesFalsos() {
        when(catalogoMensajes.texto("MENSAJE_INVALIDO")).thenReturn("El mensaje no es válido");
        when(catalogoMensajes.texto("DESTINATARIO_NO_EXISTE")).thenReturn("El destinatario no existe");
        when(catalogoMensajes.texto("TIPO_ARCHIVO_NO_PERMITIDO")).thenReturn("El tipo de archivo no está permitido");
        when(catalogoMensajes.texto("SESION_INVALIDA")).thenReturn("Debes iniciar sesión");
        when(catalogoMensajes.texto("SIN_PERMISO")).thenReturn("No tienes permiso para hacer esto");
        when(catalogoMensajes.texto("SOLICITUD_ENVIADA")).thenReturn("Tu solicitud fue enviada");
        when(catalogoMensajes.texto("CUENTA_APROBADA")).thenReturn("Cuenta aprobada");
        when(catalogoMensajes.texto("SOLICITUD_PENDIENTE")).thenReturn("Tu solicitud sigue pendiente de aprobación");
        when(catalogoMensajes.texto("CUENTA_NO_EXISTE"))
                .thenReturn("No encontramos una cuenta con ese correo. Puedes solicitarla");
        when(correo.canal()).thenReturn("correo");
        when(correo.enviar(any(), any())).thenReturn(ResultadoEnvio.ENVIADO);
        when(whatsapp.canal()).thenReturn("whatsapp");
        when(whatsapp.enviar(any(), any())).thenReturn(ResultadoEnvio.ENVIADO);
    }

    private final AtomicInteger contador = new AtomicInteger(1000);

    private record Sesion(UUID id, String token) {
    }

    private String ingresar(String correo, String clave) throws Exception {
        String cuerpo = convertidorJson.writeValueAsString(Map.of("correo", correo, "clave", clave));
        String respuesta = mockMvc.perform(post("/sesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return convertidorJson.readTree(respuesta).get("token").asText();
    }

    private Sesion crearUsuario(String nombre, String telefono) throws Exception {
        String correoUnico = nombre.toLowerCase() + "." + "prueba" + contador.incrementAndGet() + "@uco.net.co";
        String cuerpo = convertidorJson.writeValueAsString(
                Map.of("nombre", nombre, "correo", correoUnico, "telefono", telefono, "rol", "ESTUDIANTE"));
        mockMvc.perform(post("/cuentas/solicitudes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());

        UsuarioEntidad pendiente = usuarios.findByCorreo(correoUnico).orElseThrow();
        pendiente.setEstado(EstadoCuenta.ACTIVO);
        pendiente.setClaveCifrada(cifrador.cifrar("ClaveDePrueba1"));
        usuarios.saveAndFlush(pendiente);

        return new Sesion(pendiente.getId(), ingresar(correoUnico, "ClaveDePrueba1"));
    }

    private MockMultipartHttpServletRequestBuilder armarPublicacion(Sesion autor, UUID destinatarioId, String mensaje) {
        return (MockMultipartHttpServletRequestBuilder) multipart("/kudos")
                .header("Authorization", "Bearer " + autor.token())
                .param("destinatarioId", destinatarioId.toString())
                .param("mensaje", mensaje);
    }

    private UUID publicar(Sesion autor, UUID destinatarioId, String mensaje) throws Exception {
        String respuesta = mockMvc.perform(armarPublicacion(autor, destinatarioId, mensaje))
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
        Sesion autor = crearUsuario("Ana", "3000000001");
        Sesion destinatario = crearUsuario("Luis", "3000000002");
        MockMultipartFile adjunto = new MockMultipartFile(
                "archivos", "nota.pdf", "application/pdf", "contenido de prueba".getBytes());

        String respuesta = mockMvc.perform(
                        armarPublicacion(autor, destinatario.id(), "Gracias por ayudarme con el proyecto").file(adjunto))
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
    void listaSoloLosKudosDelUsuarioConSesionConPaginacionPorCursor() throws Exception {
        Sesion autor = crearUsuario("Carlos", "3000000003");
        Sesion destinatario = crearUsuario("Marta", "3000000004");
        for (int numero = 1; numero <= 3; numero++) {
            publicar(autor, destinatario.id(), "Kudo numero " + numero);
            Thread.sleep(30);
        }

        String primera = mockMvc.perform(get("/kudos")
                        .header("Authorization", "Bearer " + destinatario.token())
                        .param("limite", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kudos.length()").value(2))
                .andExpect(jsonPath("$.kudos[0].mensaje").value("Kudo numero 3"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String cursor = convertidorJson.readTree(primera).get("siguienteCursor").asText();

        mockMvc.perform(get("/kudos")
                        .header("Authorization", "Bearer " + destinatario.token())
                        .param("limite", "2")
                        .param("cursor", cursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kudos.length()").value(1))
                .andExpect(jsonPath("$.kudos[0].mensaje").value("Kudo numero 1"))
                .andExpect(jsonPath("$.siguienteCursor").doesNotExist());
    }

    @Test
    void muestraElKudoNuevoAunqueLaListaYaEstuvieraEnCache() throws Exception {
        Sesion autor = crearUsuario("Pedro", "3000000005");
        Sesion destinatario = crearUsuario("Sofia", "3000000006");

        mockMvc.perform(get("/kudos").header("Authorization", "Bearer " + destinatario.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kudos.length()").value(0));

        publicar(autor, destinatario.id(), "Excelente trabajo");

        mockMvc.perform(get("/kudos").header("Authorization", "Bearer " + destinatario.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kudos.length()").value(1));
    }

    @Test
    void rechazaUnMensajeVacio() throws Exception {
        Sesion autor = crearUsuario("Ines", "3000000007");
        Sesion destinatario = crearUsuario("Jorge", "3000000008");

        mockMvc.perform(armarPublicacion(autor, destinatario.id(), ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El mensaje no es válido"));
    }

    @Test
    void rechazaUnArchivoDeUnTipoNoPermitido() throws Exception {
        Sesion autor = crearUsuario("Rosa", "3000000009");
        Sesion destinatario = crearUsuario("Tomas", "3000000010");
        MockMultipartFile peligroso = new MockMultipartFile(
                "archivos", "virus.exe", "application/x-msdownload", "MZ".getBytes());

        mockMvc.perform(armarPublicacion(autor, destinatario.id(), "Con archivo peligroso").file(peligroso))
                .andExpect(status().isBadRequest());
    }

    @Test
    void respondeNoEncontradoSiElDestinatarioNoExiste() throws Exception {
        Sesion autor = crearUsuario("Elena", "3000000011");

        mockMvc.perform(armarPublicacion(autor, UUID.randomUUID(), "Hola"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("El destinatario no existe"));
    }

    @Test
    void noDejaPublicarSinSesion() throws Exception {
        mockMvc.perform(multipart("/kudos")
                        .param("destinatarioId", UUID.randomUUID().toString())
                        .param("mensaje", "Hola"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Debes iniciar sesión"));
    }

    @Test
    void elPanelDeSolicitudesSoloLoVeElAdministrador() throws Exception {
        Sesion estudiante = crearUsuario("Nora", "3000000012");

        mockMvc.perform(get("/cuentas/solicitudes").header("Authorization", "Bearer " + estudiante.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No tienes permiso para hacer esto"));

        String tokenAdmin = ingresar(correoAdmin, claveAdmin);
        mockMvc.perform(get("/cuentas/solicitudes").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    void elAdministradorApruebaUnaSolicitudYLaPersonaPuedeIngresar() throws Exception {
        String correoNuevo = "camila.rios" + contador.incrementAndGet() + "@uco.net.co";
        String solicitud = convertidorJson.writeValueAsString(
                Map.of("nombre", "Camila Rios", "correo", correoNuevo, "telefono", "3000000013", "rol", "PROFESOR"));
        mockMvc.perform(post("/cuentas/solicitudes").contentType(MediaType.APPLICATION_JSON).content(solicitud))
                .andExpect(status().isCreated());

        String cuerpoIngreso = convertidorJson.writeValueAsString(Map.of("correo", correoNuevo, "clave", "x"));
        mockMvc.perform(post("/sesion").contentType(MediaType.APPLICATION_JSON).content(cuerpoIngreso))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Tu solicitud sigue pendiente de aprobación"));

        UUID id = usuarios.findByCorreo(correoNuevo).orElseThrow().getId();
        String tokenAdmin = ingresar(correoAdmin, claveAdmin);
        mockMvc.perform(post("/cuentas/solicitudes/" + id + "/aprobar").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());

        assertThat(usuarios.findByCorreo(correoNuevo).orElseThrow().getEstado()).isEqualTo(EstadoCuenta.ACTIVO);
        verify(avisos).cuentaAprobada(any(), any());
    }

    @Test
    void avisaCuandoNoExisteLaCuenta() throws Exception {
        String cuerpo = convertidorJson.writeValueAsString(
                Map.of("correo", "nadie.nadie0000@uco.net.co", "clave", "x"));

        mockMvc.perform(post("/sesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No encontramos una cuenta con ese correo. Puedes solicitarla"));
    }

    @Test
    void respondeQueEstaVivaEnSalud() throws Exception {
        mockMvc.perform(get("/salud"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ok"));
    }
}
