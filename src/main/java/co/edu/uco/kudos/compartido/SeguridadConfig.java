package co.edu.uco.kudos.compartido;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import co.edu.uco.kudos.cuentas.aplicacion.puertos.Sesiones;
import co.edu.uco.kudos.cuentas.infraestructura.FiltroSesion;
import co.edu.uco.kudos.kudos.aplicacion.puertos.UsuarioRepositorio;

@Configuration
public class SeguridadConfig {

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    @Bean
    SecurityFilterChain cadenaDeSeguridad(
            HttpSecurity http,
            Sesiones sesiones,
            UsuarioRepositorio usuarios,
            RespuestasDeSeguridad respuestas) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(respuestas)
                        .accessDeniedHandler(respuestas))
                .addFilterBefore(new FiltroSesion(sesiones, usuarios), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(peticiones -> peticiones
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/salud", "/archivos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/sesion", "/cuentas/solicitudes").permitAll()
                        .requestMatchers("/cuentas/**").hasRole(ROL_ADMINISTRADOR)
                        .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(ConfiguracionKudos configuracion) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(configuracion.cors().origenesPermitidos());
        cors.setAllowedMethods(List.of("GET", "POST", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", cors);
        return fuente;
    }
}