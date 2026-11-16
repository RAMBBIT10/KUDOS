package co.edu.uco.kudos.compartido;

public final class ConfiguracionDePrueba {

    private ConfiguracionDePrueba() {
    }

    public static ConfiguracionKudos con(String urlStrapi, String tokenStrapi) {
        return new ConfiguracionKudos(
                null,
                new ConfiguracionKudos.Cache(60),
                null,
                null,
                null,
                null,
                new ConfiguracionKudos.Canales("correo", "whatsapp", "push"),
                new ConfiguracionKudos.Strapi(
                        urlStrapi,
                        tokenStrapi,
                        new ConfiguracionKudos.ColeccionMensajes("/api/mensajes", "codigo", "texto"),
                        new ConfiguracionKudos.ColeccionNotificaciones(
                                "/api/notificaciones", "canal", "asunto", "cuerpo")),
                new ConfiguracionKudos.Cuentas(
                        "uco.net.co", 4, 10, 3600, "sesion:", "Admin", "admin.prueba0000@uco.net.co", "",
                        "clave_admin", "cuenta-solicitud", "cuenta-aprobada", "cuenta-rechazada",
                        "cuenta-aprobada-whatsapp"));
    }

    public static ConfiguracionKudos sinStrapi() {
        return con("http://localhost", "");
    }
}
