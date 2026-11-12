package co.edu.uco.kudos;

import co.edu.uco.kudos.compartido.ConfiguracionKudos;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ConfiguracionKudos.class)
public class KudosApplication {

    public static void main(String[] argumentos) {
        SpringApplication.run(KudosApplication.class, argumentos);
    }
}
