package co.edu.uco.kudos.cuentas.dominio;

import java.util.regex.Pattern;

public class ReglasCuenta {

    private static final int LARGO_MAXIMO_NOMBRE = 100;

    private static final Pattern TELEFONO_LOCAL = Pattern.compile("^[0-9]{10}$");

    private static final int LARGO_MAXIMO_CORREO = 254;
    private static final String CARACTERES_USUARIO = "._%+-";

    private final String prefijoTelefono;
    private final int largoMinimoClave;

    public ReglasCuenta(String prefijoTelefono, int largoMinimoClave) {
        this.prefijoTelefono = prefijoTelefono;
        this.largoMinimoClave = largoMinimoClave;
    }

    public boolean claveEsValida(String clave) {
        return clave != null && clave.length() >= largoMinimoClave;
    }

    public boolean correoEsValido(String correo) {
        if (correo == null) {
            return false;
        }
        String limpio = correo.trim();
        int posicionArroba = limpio.indexOf('@');
        if (limpio.length() > LARGO_MAXIMO_CORREO || posicionArroba <= 0 || posicionArroba != limpio.lastIndexOf('@')) {
            return false;
        }
        return usuarioEsValido(limpio.substring(0, posicionArroba)) && dominioEsValido(limpio.substring(posicionArroba + 1));
    }

    private boolean usuarioEsValido(String usuario) {
        for (char letra : usuario.toCharArray()) {
            if (!esLetraONumero(letra) && CARACTERES_USUARIO.indexOf(letra) < 0) {
                return false;
            }
        }
        return true;
    }

    private boolean dominioEsValido(String dominio) {
        int ultimoPunto = dominio.lastIndexOf('.');
        if (ultimoPunto <= 0 || dominio.length() - ultimoPunto - 1 < 2) {
            return false;
        }
        for (int posicion = 0; posicion < dominio.length(); posicion++) {
            char letra = dominio.charAt(posicion);
            boolean esPunto = letra == '.';
            if (!esLetraONumero(letra) && letra != '-' && !esPunto) {
                return false;
            }
            if (esPunto && (posicion == 0 || dominio.charAt(posicion - 1) == '.')) {
                return false;
            }
        }
        return dominio.substring(ultimoPunto + 1).chars().allMatch(letra -> esLetraONumero((char) letra) && !Character.isDigit(letra));
    }

    private boolean esLetraONumero(char letra) {
        return (letra >= 'a' && letra <= 'z') || (letra >= 'A' && letra <= 'Z') || (letra >= '0' && letra <= '9');
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
