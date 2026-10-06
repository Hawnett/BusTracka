package pe.edu.ulima.is2.bustracka.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.ulima.is2.bustracka.security.JwtAuthFilter;

/** Registra el JwtAuthFilter como Servlet Filter dentro del contenedor Tomcat embebido. */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter filtro) {
        FilterRegistrationBean<JwtAuthFilter> registro = new FilterRegistrationBean<>(filtro);
        registro.addUrlPatterns("/api/*");
        registro.setOrder(1);
        return registro;
    }
}
