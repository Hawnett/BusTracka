package pe.edu.ulima.is2.bustracka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Habilita CORS para que el frontend React (servido por Vite en
 * http://localhost:5173 durante el desarrollo) pueda consumir la API
 * sin ser bloqueado por el navegador. Otros origenes: variable CORS_ORIGINS.
 *
 * Es un filtro que corre antes que JwtAuthFilter: asi tambien las respuestas
 * 401/403 del filtro llevan las cabeceras CORS y el frontend puede leerlas.
 */
@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter(@Value("${app.cors-origins}") List<String> origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenes);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);

        FilterRegistrationBean<CorsFilter> registro = new FilterRegistrationBean<>(new CorsFilter(fuente));
        registro.addUrlPatterns("/api/*");
        registro.setOrder(0);
        return registro;
    }
}
