package co.edu.uco.kudos.cuentas.infraestructura;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.cuentas.aplicacion.puertos.GeneradorClaves;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class GeneradorClavesSeguras implements GeneradorClaves {

    private static final String CARACTERES = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final SecureRandom azar = new SecureRandom();
    private final int longitud;

    public GeneradorClavesSeguras(ConfiguracionKudos configuracion) {
        this.longitud = configuracion.cuentas().longitudClaveTemporal();
    }

    @Override
    public String nueva() {
        StringBuilder clave = new StringBuilder();
        for (int posicion = 0; posicion < longitud; posicion++) {
            clave.append(CARACTERES.charAt(azar.nextInt(CARACTERES.length())));
        }
        return clave.toString();
    }
}
