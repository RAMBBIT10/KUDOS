package co.edu.uco.kudos.catalogos.infraestructura;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

class ServidorStrapiFalso implements AutoCloseable {

    private final HttpServer servidor;
    private final AtomicInteger peticiones = new AtomicInteger();
    private volatile int estado = 200;
    private volatile String respuesta = "{\"data\":[]}";
    private volatile String ultimaRuta = "";
    private volatile String ultimaConsulta = "";
    private volatile String ultimaAutorizacion = "";

    ServidorStrapiFalso() {
        try {
            servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException error) {
            throw new IllegalStateException(error);
        }
        servidor.createContext("/", this::atender);
        servidor.start();
    }

    String url() {
        return "http://localhost:" + servidor.getAddress().getPort();
    }

    void responder(int estado, String respuesta) {
        this.estado = estado;
        this.respuesta = respuesta;
    }

    int peticiones() {
        return peticiones.get();
    }

    String ultimaRuta() {
        return ultimaRuta;
    }

    String ultimaConsulta() {
        return ultimaConsulta;
    }

    String ultimaAutorizacion() {
        return ultimaAutorizacion;
    }

    private void atender(HttpExchange intercambio) throws IOException {
        peticiones.incrementAndGet();
        ultimaRuta = intercambio.getRequestURI().getPath();
        ultimaConsulta = String.valueOf(intercambio.getRequestURI().getQuery());
        ultimaAutorizacion = String.valueOf(intercambio.getRequestHeaders().getFirst("Authorization"));

        byte[] cuerpo = respuesta.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().add("Content-Type", "application/json");
        intercambio.sendResponseHeaders(estado, cuerpo.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(cuerpo);
        }
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}
