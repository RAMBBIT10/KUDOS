package co.edu.uco.kudos.kudos.infraestructura.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaludControlador {

    @GetMapping("/salud")
    public Map<String, String> revisar() {
        return Map.of("estado", "ok");
    }
}
