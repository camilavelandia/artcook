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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          UserDetailsService userDetailsService) {

        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // CORS
            .cors(Customizer.withDefaults())

            // CSRF desactivado para APIs REST con JWT
            .csrf(csrf -> csrf.disable())

            // Sesiones stateless
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Permisos
            .authorizeHttpRequests(auth -> auth

                // =========================
                // PUBLICOS
                // =========================
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

                // Permitir preflight OPTIONS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // =========================
                // ADMIN
                // =========================
                .requestMatchers(
                    "/usuario/mostrartodo",
                    "/usuario/eliminar",
                    "/usuario/estado",
                    "/usuario/porrol",
                    "/usuario/contar",
                    "/usuario/existe",
                    "/auditoria/**"
                ).hasRole("ADMIN")

                // =========================
                // USER y ADMIN
                // =========================
                .requestMatchers(
                    "/usuario/poremail",
                    "/usuario/actualizar",
                    "/receta/**",
                    "/ia/**",
                    "/video/**"
                ).hasAnyRole("USER", "ADMIN")

                // Todo lo demás requiere auth
                .anyRequest().authenticated()
            )

            // Provider
            .authenticationProvider(authenticationProvider())

            // JWT Filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Frontends permitidos
        config.setAllowedOrigins(List.of(
            "http://localhost:4200",
            "http://localhost:4201"
            // Agrega aquí Netlify luego
        ));

        // Métodos permitidos
        config.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS"
        ));

        // Headers permitidos
        config.setAllowedHeaders(List.of("*"));

        // Permitir credenciales
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
