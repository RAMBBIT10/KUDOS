package co.edu.uco.kudos.catalogos.aplicacion;

import java.util.Map;

public record PlantillaNotificacion(String asunto, String cuerpo) {

    public String cuerpoPara(String nombre, String mensaje) {
        return rellenar(Map.of("nombre", nombre, "mensaje", mensaje));
    }

    public String rellenar(Map<String, String> valores) {
        String texto = cuerpo;
        for (Map.Entry<String, String> valor : valores.entrySet()) {
            texto = texto.replace("{" + valor.getKey() + "}", valor.getValue());
        }
        return texto;
    }
}
