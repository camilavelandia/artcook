package co.edu.unbosque.artcook.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Clase de configuración de seguridad de la aplicación ArtCook.
 * Define las reglas de acceso a los endpoints, la política de sesiones,
 * la configuración de CORS y el proveedor de autenticación con JWT.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Filtro de autenticación JWT que se ejecuta en cada solicitud. */
    private final JwtAuthenticationFilter jwtAuthFilter;

    /** Servicio para cargar los detalles del usuario durante la autenticación. */
    private final UserDetailsService userDetailsService;

    /**
     * Constructor que inicializa las dependencias necesarias para la configuración de seguridad.
     *
     * @param jwtAuthFilter      filtro de autenticación JWT
     * @param userDetailsService servicio para cargar los detalles del usuario
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     * Define los endpoints públicos, los restringidos por rol y la política de sesiones sin estado.
     *
     * @param http objeto de configuración de seguridad HTTP
     * @return cadena de filtros de seguridad configurada
     * @throws Exception si ocurre un error durante la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                    "/usuario/registrar",
                    "/usuario/login",
                    "/usuario/verificar",
                    "/usuario/recuperar",
                    "/usuario/cambiarcontrasena",
                    "/videos/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                .requestMatchers(
                    "/usuario/mostrartodo",
                    "/usuario/eliminar",
                    "/usuario/estado",
                    "/usuario/porrol",
                    "/usuario/contar",
                    "/usuario/existe",
                    "/auditoria/**"
                ).hasRole("ADMIN")
                .requestMatchers(
                    "/usuario/poremail",
                    "/usuario/actualizar",
                    "/receta/**",
                    "/ia/**",
                    "/video/**"
                ).hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura la fuente de configuración CORS de la aplicación.
     * Permite solicitudes desde los orígenes del frontend en desarrollo.
     *
     * @return fuente de configuración CORS con los orígenes y métodos permitidos
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:4200",
            "https://artcooks.netlify.app/"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Configura el proveedor de autenticación con el servicio de usuarios y el codificador de contraseñas.
     *
     * @return proveedor de autenticación configurado
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expone el gestor de autenticación como bean de Spring.
     *
     * @param config configuración de autenticación de Spring
     * @return gestor de autenticación
     * @throws Exception si ocurre un error al obtener el gestor
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Crea el codificador de contraseñas usando BCrypt.
     *
     * @return codificador de contraseñas BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}