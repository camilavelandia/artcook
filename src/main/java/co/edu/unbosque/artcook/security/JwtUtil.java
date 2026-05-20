package co.edu.unbosque.artcook.security;

import co.edu.unbosque.artcook.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Clase utilitaria para operaciones con JSON Web Tokens (JWT).
 * Proporciona métodos para generar, validar y extraer información de tokens JWT
 * usados en la autenticación de usuarios de ArtCook.
 */
@Component
public class JwtUtil {

    /** Tiempo de validez del token JWT en milisegundos (24 horas). */
    private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000;

    /**
     * Clave secreta utilizada para firmar los tokens JWT.
     * Se configura en application.properties con la propiedad jwt.secret.
     */
    @Value("${jwt.secret:artcookDefaultSecretKeyWhichShouldBeAtLeast32Chars}")
    private String secret;

    /**
     * Obtiene la clave de firma para los tokens JWT.
     *
     * @return clave de firma generada a partir del secreto configurado
     */
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el email del usuario del token JWT.
     *
     * @param token token JWT del cual extraer el email
     * @return email del usuario contenido en el token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token JWT.
     *
     * @param token token JWT del cual extraer la fecha de expiración
     * @return fecha de expiración del token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae el rol del usuario del token JWT.
     *
     * @param token token JWT del cual extraer el rol
     * @return rol del usuario contenido en el token (ADMIN o USER)
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extrae el ID del usuario del token JWT.
     *
     * @param token token JWT del cual extraer el ID
     * @return ID del usuario contenido en el token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Método genérico para extraer cualquier claim del token JWT.
     *
     * @param token          token JWT del cual extraer el claim
     * @param claimsResolver función para resolver el claim específico
     * @return valor del claim extraído
     * @param <T> tipo de dato del claim a extraer
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token JWT.
     *
     * @param token token JWT del cual extraer todos los claims
     * @return objeto Claims con todos los claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Verifica si un token JWT ha expirado.
     *
     * @param token token JWT a verificar
     * @return true si el token ha expirado, false en caso contrario
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Genera un token JWT para un usuario de ArtCook.
     * Incluye el email, rol e ID del usuario como claims.
     *
     * @param userDetails detalles del usuario para el cual generar el token
     * @return token JWT generado
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities());

        if (userDetails instanceof Usuario usuario) {
            claims.put("role", usuario.getRol().name());
            claims.put("userId", usuario.getId());
            claims.put("nombre", usuario.getNombre());
        }

        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Crea un token JWT con los claims especificados.
     *
     * @param claims  claims a incluir en el token
     * @param subject asunto del token (email del usuario)
     * @return token JWT creado
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida un token JWT para un usuario específico.
     *
     * @param token       token JWT a validar
     * @param userDetails detalles del usuario contra los cuales validar el token
     * @return true si el token es válido para el usuario, false en caso contrario
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}