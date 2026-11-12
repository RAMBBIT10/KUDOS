package co.edu.uco.kudos.compartido;

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
        return responder(HttpStatus.NOT_FOUND, error.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> conflicto() {
        return responder(HttpStatus.CONFLICT, "Ya existe un registro con esos datos");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Map<String, String>> metodoNoPermitido() {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Metodo no permitido");
    }

    private String mensajeSeguro(Exception error) {
        if (error instanceof ReglaDeNegocioException) {
            return error.getMessage();
        }
        if (error instanceof MaxUploadSizeExceededException) {
            return "El archivo supera el tamano permitido";
        }
        return "La peticion no es valida";
    }

    private ResponseEntity<Map<String, String>> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
