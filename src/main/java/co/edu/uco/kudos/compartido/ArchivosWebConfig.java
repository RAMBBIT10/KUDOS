package co.edu.uco.kudos.compartido;

import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ArchivosWebConfig implements WebMvcConfigurer {

    private final ConfiguracionKudos configuracion;

    public ArchivosWebConfig(ConfiguracionKudos configuracion) {
        this.configuracion = configuracion;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registro) {
        String carpeta = Path.of(configuracion.archivos().carpeta()).toAbsolutePath().toUri().toString();
        registro.addResourceHandler("/archivos/**").addResourceLocations(carpeta);
    }
}
