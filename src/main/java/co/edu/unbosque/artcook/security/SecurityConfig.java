package co.edu.unbosque.artcook.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Clase de configuración de seguridad para ArtCook.
 * Configura la autenticación y autorización basada en JWT.
 * Define qué endpoints son públicos y cuáles requieren autenticación o rol específico.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Filtro de autenticación JWT que procesa los tokens en las solicitudes. */
    private final JwtAuthenticationFilter jwtAuthFilter;

    /** Servicio que carga los detalles del usuario para la autenticación. */
    private final UserDetailsService userDetailsService;

    /**
     * Constructor que inicializa los componentes necesarios para la seguridad.
     *
     * @param jwtAuthFilter      filtro para procesar tokens JWT
     * @param userDetailsService servicio para cargar detalles de usuarios
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
            UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     * Define reglas de acceso por rol, manejo de sesiones sin estado y filtros JWT.
     *
     * @param http configuración de seguridad HTTP
     * @return cadena de filtros de seguridad configurada
     * @throws Exception si ocurre un error durante la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos — no requieren autenticación
                        .requestMatchers(
                                "/usuario/registrar",
                                "/usuario/login",
                                "/usuario/verificar",
                                "/usuario/recuperar",
                                "/usuario/cambiarcontrasena"
                        ).permitAll()
                        // Swagger — público para documentación
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Endpoints solo para ADMIN
                        .requestMatchers(
                                "/usuario/mostrartodo",
                                "/usuario/eliminar",
                                "/usuario/estado",
                                "/usuario/porrol",
                                "/usuario/contar",
                                "/usuario/existe",
                                "/auditoria/**",
                                "/receta/mostrartodo"
                        ).hasRole("ADMIN")
                        // Endpoints para usuarios autenticados (USER o ADMIN)
                        .requestMatchers(
                                "/usuario/poremail",
                                "/usuario/actualizar",
                                "/receta/**",
                                "/ia/**"
                        ).hasAnyRole("USER", "ADMIN")
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura el proveedor de autenticación.
     * Establece el servicio de detalles de usuario y el codificador de contraseñas.
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
     * Configura el gestor de autenticación.
     *
     * @param config configuración de autenticación
     * @return gestor de autenticación
     * @throws Exception si ocurre un error durante la configuración
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configura el codificador de contraseñas.
     * Utiliza BCrypt para el hash seguro de contraseñas.
     *
     * @return codificador de contraseñas BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}