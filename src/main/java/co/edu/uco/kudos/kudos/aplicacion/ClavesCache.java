package co.edu.uco.kudos.kudos.aplicacion;

import java.util.UUID;

public final class ClavesCache {

    private ClavesCache() {
    }

    public static String prefijoDelDestinatario(UUID destinatarioId) {
        return "kudos:" + destinatarioId;
    }

    public static String listaDeKudos(UUID destinatarioId, int limite, String cursor) {
        String cursorTexto = cursor == null ? "inicio" : cursor;
        return prefijoDelDestinatario(destinatarioId) + ":" + limite + ":" + cursorTexto;
    }
}
