package co.edu.uco.kudos.kudos.dominio;

public final class ReglasMensaje {

    public static final int LIMITE_CARACTERES = 500;

    private ReglasMensaje() {
    }

    public static boolean mensajeEsValido(String mensaje) {
        if (mensaje == null) {
            return false;
        }
        String texto = mensaje.trim();
        return !texto.isEmpty() && texto.length() <= LIMITE_CARACTERES;
    }
}
