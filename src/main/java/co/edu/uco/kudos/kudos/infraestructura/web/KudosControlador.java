package co.edu.uco.kudos.kudos.infraestructura.web;

import co.edu.uco.kudos.kudos.aplicacion.ListarKudos;
import co.edu.uco.kudos.kudos.aplicacion.PeticionPublicarKudo;
import co.edu.uco.kudos.kudos.aplicacion.PublicarKudo;
import co.edu.uco.kudos.kudos.aplicacion.RespuestaListaKudos;
import co.edu.uco.kudos.kudos.dominio.Kudo;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/kudos")
public class KudosControlador {

    private static final int LIMITE_MINIMO = 1;
    private static final int LIMITE_MAXIMO = 50;

    private final PublicarKudo publicarKudo;
    private final ListarKudos listarKudos;
    private final AlmacenArchivos almacenArchivos;

    public KudosControlador(PublicarKudo publicarKudo, ListarKudos listarKudos, AlmacenArchivos almacenArchivos) {
        this.publicarKudo = publicarKudo;
        this.listarKudos = listarKudos;
        this.almacenArchivos = almacenArchivos;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Kudo publicar(
            @RequestParam UUID autorId,
            @RequestParam UUID destinatarioId,
            @RequestParam String mensaje,
            @RequestParam(name = "archivos", required = false) List<MultipartFile> archivos) {
        var adjuntos = almacenArchivos.guardar(archivos);
        return publicarKudo.ejecutar(new PeticionPublicarKudo(autorId, destinatarioId, mensaje, adjuntos));
    }

    @GetMapping
    public RespuestaListaKudos listar(
            @RequestParam UUID destinatarioId,
            @RequestParam(defaultValue = "10") int limite,
            @RequestParam(required = false) String cursor) {
        int limiteAjustado = Math.min(Math.max(limite, LIMITE_MINIMO), LIMITE_MAXIMO);
        return listarKudos.ejecutar(destinatarioId, limiteAjustado, cursor);
    }
}
