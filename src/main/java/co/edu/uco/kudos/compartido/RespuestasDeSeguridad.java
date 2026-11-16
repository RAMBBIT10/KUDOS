package co.edu.uco.kudos.compartido;

import co.edu.uco.kudos.catalogos.aplicacion.CatalogoMensajes;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RespuestasDeSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final CatalogoMensajes catalogo;
    private final ObjectMapper convertidorJson;

    public RespuestasDeSeguridad(CatalogoMensajes catalogo, ObjectMapper convertidorJson) {
        this.catalogo = catalogo;
        this.convertidorJson = convertidorJson;
    }

    @Override
    public void commence(HttpServletRequest peticion, HttpServletResponse respuesta, AuthenticationException error)
            throws IOException {
        escribir(respuesta, HttpStatus.UNAUTHORIZED, "SESION_INVALIDA");
    }

    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta, AccessDeniedException error)
            throws IOException {
        escribir(respuesta, HttpStatus.FORBIDDEN, "SIN_PERMISO");
    }

    private void escribir(HttpServletResponse respuesta, HttpStatus estado, String codigo) throws IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        convertidorJson.writeValue(respuesta.getWriter(), Map.of("mensaje", catalogo.texto(codigo)));
    }
}
