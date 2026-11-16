package co.edu.uco.kudos.cuentas.dominio;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReglasCuenta {

    private static final int LARGO_MAXIMO_NOMBRE = 100;

    private static final Pattern TELEFONO_LOCAL = Pattern.compile("^[0-9]{10}$");

    private final Pattern correoInstitucional;
    private final String prefijoTelefono;

    public ReglasCuenta(List<String> dominios, int digitosDocumento, String prefijoTelefono) {
        this.prefijoTelefono = prefijoTelefono;
        String dominiosPermitidos = dominios.stream()
                .map(String::trim)
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        this.correoInstitucional = Pattern.compile(
                "^[a-z]+\\.[a-z]+[0-9]{" + digitosDocumento + "}@(" + dominiosPermitidos + ")$");
    }

    public boolean correoEsValido(String correo) {
        return correo != null && correoInstitucional.matcher(correo).matches();
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
