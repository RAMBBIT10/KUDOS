package co.edu.uco.kudos.cuentas.infraestructura;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import co.edu.uco.kudos.cuentas.dominio.ReglasCuenta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CuentasConfig {

    @Bean
    ReglasCuenta reglasCuenta(ConfiguracionKudos configuracion) {
        ConfiguracionKudos.Cuentas datos = configuracion.cuentas();
        return new ReglasCuenta(datos.dominiosCorreo(), datos.digitosDocumento(), datos.prefijoTelefono());
    }
}
