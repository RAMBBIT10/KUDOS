package co.edu.uco.kudos.notificaciones.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClienteWhatsappTest {

    private HttpServer servidor;
    private final AtomicReference<String> rutaRecibida = new AtomicReference<>();
    private final AtomicReference<String> cuerpoRecibido = new AtomicReference<>();
    private final AtomicReference<String> autorizacionRecibida = new AtomicReference<>();
    private int estadoDeRespuesta = 201;

    @BeforeEach
    void levantarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", intercambio -> {
            rutaRecibida.set(intercambio.getRequestURI().getPath());
            autorizacionRecibida.set(intercambio.getRequestHeaders().getFirst("Authorization"));
            cuerpoRecibido.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            intercambio.sendResponseHeaders(estadoDeRespuesta, -1);
            intercambio.close();
        });
        servidor.start();
    }

    @AfterEach
    void apagarServidor() {
        servidor.stop(0);
    }

    private ClienteWhatsapp crearCliente() {
        String urlBase = "http://127.0.0.1:" + servidor.getAddress().getPort();
        ConfiguracionKudos.Whatsapp datos = new ConfiguracionKudos.Whatsapp(
                urlBase, "/cuentas/{sid}/mensajes", "whatsapp:", "AC123", "token123", "+14155238886");
        ConfiguracionKudos configuracion = new ConfiguracionKudos(null, null, null, datos, null, null, null, null, null);
        return new ClienteWhatsapp(configuracion);
    }

    @Test
    void enviaElFormularioConOrigenDestinoYTexto() {
        crearCliente().enviar("+573000000002", "Hola");

        assertThat(rutaRecibida.get()).isEqualTo("/cuentas/AC123/mensajes");
        assertThat(autorizacionRecibida.get()).startsWith("Basic ");
        assertThat(cuerpoRecibido.get())
                .contains("Body=Hola")
                .contains("From=whatsapp%3A%2B14155238886")
                .contains("To=whatsapp%3A%2B573000000002");
    }

    @Test
    void lanzaErrorSiElProveedorRechazaElEnvio() {
        estadoDeRespuesta = 400;

        assertThatThrownBy(() -> crearCliente().enviar("+573000000002", "Hola"))
                .isInstanceOf(RuntimeException.class);
    }
}
