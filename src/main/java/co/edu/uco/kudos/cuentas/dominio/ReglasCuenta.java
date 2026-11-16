package co.edu.uco.kudos.cuentas.dominio;

import java.util.regex.Pattern;

public class ReglasCuenta {

    private static final int LARGO_MAXIMO_NOMBRE = 100;

    private static final Pattern TELEFONO_LOCAL = Pattern.compile("^[0-9]{10}$");

    private static final Pattern CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    private final String prefijoTelefono;

    public ReglasCuenta(String prefijoTelefono) {
        this.prefijoTelefono = prefijoTelefono;
    }

    public boolean correoEsValido(String correo) {
        return correo != null && CORREO.matcher(correo.trim()).matches();
    }

    public String normalizarTelefono(String telefono) {
        return prefijoTelefono + limpiarTelefono(telefono);
    }

    public boolean telefonoEsValido(String telefono) {
        return telefono != null && TELEFONO_LOCAL.matcher(limpiarTelefono(telefono)).matches();
    }

    private String limpiarTelefono(String telefono) {
        return telefono.replaceAll("[\\s-]", "");
    }

    public boolean nombreEsValido(String nombre) {
        return nombre != null && !nombre.isBlank() && nombre.trim().length() <= LARGO_MAXIMO_NOMBRE;
    }
}
