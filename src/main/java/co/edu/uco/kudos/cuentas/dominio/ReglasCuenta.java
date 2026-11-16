package co.edu.uco.kudos.cuentas.dominio;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReglasCuenta {

    private static final Pattern TELEFONO = Pattern.compile("^\\+[0-9]{8,15}$");
    private static final int LARGO_MAXIMO_NOMBRE = 100;

    private final Pattern correoInstitucional;

    public ReglasCuenta(List<String> dominios, int digitosDocumento) {
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

    public boolean telefonoEsValido(String telefono) {
        return telefono != null && TELEFONO.matcher(telefono).matches();
    }

    public boolean nombreEsValido(String nombre) {
        return nombre != null && !nombre.isBlank() && nombre.trim().length() <= LARGO_MAXIMO_NOMBRE;
    }
}
