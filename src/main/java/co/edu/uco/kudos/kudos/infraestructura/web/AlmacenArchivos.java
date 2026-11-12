package co.edu.uco.kudos.kudos.infraestructura.web;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.kudos.aplicacion.ReglaDeNegocioException;
import co.edu.uco.kudos.kudos.dominio.Adjunto;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AlmacenArchivos {

    private final ConfiguracionKudos.Archivos reglas;

    public AlmacenArchivos(ConfiguracionKudos configuracion) {
        this.reglas = configuracion.archivos();
    }

    public List<Adjunto> guardar(List<MultipartFile> archivos) {
        List<MultipartFile> recibidos = archivos == null
                ? List.of()
                : archivos.stream().filter(archivo -> !archivo.isEmpty()).toList();

        validar(recibidos);

        List<Adjunto> adjuntos = new ArrayList<>();
        for (MultipartFile archivo : recibidos) {
            adjuntos.add(guardarUno(archivo));
        }
        return adjuntos;
    }

    private void validar(List<MultipartFile> archivos) {
        if (archivos.size() > reglas.cantidadMaxima()) {
            throw new ReglaDeNegocioException("Se permiten maximo " + reglas.cantidadMaxima() + " archivos");
        }
        for (MultipartFile archivo : archivos) {
            String tipo = archivo.getContentType();
            if (tipo == null || !reglas.tiposPermitidos().contains(tipo)) {
                throw new ReglaDeNegocioException("Tipo de archivo no permitido");
            }
            if (archivo.getSize() > reglas.tamanoMaximoEnBytes()) {
                throw new ReglaDeNegocioException("El archivo supera el tamano permitido");
            }
        }
    }

    private Adjunto guardarUno(MultipartFile archivo) {
        String nombreGuardado = UUID.randomUUID() + extensionDe(archivo.getOriginalFilename());
        try {
            Path carpeta = Path.of(reglas.carpeta()).toAbsolutePath();
            Files.createDirectories(carpeta);
            archivo.transferTo(carpeta.resolve(nombreGuardado));
        } catch (IOException error) {
            throw new UncheckedIOException("No se pudo guardar el archivo", error);
        }
        return new Adjunto(
                archivo.getOriginalFilename(),
                archivo.getContentType(),
                archivo.getSize(),
                "/archivos/" + nombreGuardado);
    }

    private String extensionDe(String nombre) {
        if (nombre == null || !nombre.contains(".")) {
            return "";
        }
        String extension = nombre.substring(nombre.lastIndexOf('.')).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,5}") ? extension : "";
    }
}
