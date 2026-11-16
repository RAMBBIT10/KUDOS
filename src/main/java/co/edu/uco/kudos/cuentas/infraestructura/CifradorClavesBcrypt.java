package co.edu.uco.kudos.cuentas.infraestructura;

import co.edu.uco.kudos.cuentas.aplicacion.puertos.CifradorClaves;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CifradorClavesBcrypt implements CifradorClaves {

    private final BCryptPasswordEncoder cifrador = new BCryptPasswordEncoder();

    @Override
    public String cifrar(String clave) {
        return cifrador.encode(clave);
    }

    @Override
    public boolean coincide(String clave, String claveCifrada) {
        return cifrador.matches(clave, claveCifrada);
    }
}
