package co.edu.uco.kudos.compartido;

import co.edu.uco.kudos.catalogos.aplicacion.CatalogoMensajes;
import co.edu.uco.kudos.kudos.aplicacion.NoEncontradoException;
import co.edu.uco.kudos.kudos.aplicacion.ReglaDeNegocioException;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ManejadorDeErrores {

    private final CatalogoMensajes catalogo;

    public ManejadorDeErrores(CatalogoMensajes catalogo) {
        this.catalogo = catalogo;
    }

    @ExceptionHandler({
            ReglaDeNegocioException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MaxUploadSizeExceededException.class,
            HttpMediaTypeNotSupportedException.class
    })
    ResponseEntity<Map<String, String>> peticionInvalida(Exception error) {
        return responder(HttpStatus.BAD_REQUEST, mensajeSeguro(error));
    }

    @ExceptionHandler(NoEncontradoException.class)
    ResponseEntity<Map<String, String>> noEncontrado(NoEncontradoException error) {
        return responder(HttpStatus.NOT_FOUND, catalogo.texto(error.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> conflicto() {
        return responder(HttpStatus.CONFLICT, catalogo.texto("REGISTRO_DUPLICADO"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Map<String, String>> metodoNoPermitido() {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, catalogo.texto("METODO_NO_PERMITIDO"));
    }

    private String mensajeSeguro(Exception error) {
        if (error instanceof ReglaDeNegocioException) {
            return catalogo.texto(error.getMessage());
        }
        if (error instanceof MaxUploadSizeExceededException) {
            return catalogo.texto("ARCHIVO_MUY_GRANDE");
        }
        return catalogo.texto("PETICION_INVALIDA");
    }

    private ResponseEntity<Map<String, String>> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
