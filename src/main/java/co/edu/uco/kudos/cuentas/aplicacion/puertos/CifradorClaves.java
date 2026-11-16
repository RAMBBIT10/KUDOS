package co.edu.uco.kudos.cuentas.aplicacion.puertos;

public interface CifradorClaves {

    String cifrar(String clave);

    boolean coincide(String clave, String claveCifrada);
}
