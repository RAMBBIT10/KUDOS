package co.edu.uco.kudos.catalogos.aplicacion;

public record PlantillaNotificacion(String asunto, String cuerpo) {

    public String cuerpoPara(String nombre, String mensaje) {
        return cuerpo.replace("{nombre}", nombre).replace("{mensaje}", mensaje);
    }
}
