package co.edu.unbosque.artcook.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Representa la entidad usuario en la base de datos.
 * Extiende Persona e implementa UserDetails de Spring Security
 * para integrarse con el sistema de autenticación JWT.
 */
@Entity
@Table(name = "usuarios")
public class Usuario extends Persona implements UserDetails {

    @Enumerated(EnumType.STRING)
    private RolUsuario rol;

    private boolean emailVerificado;

    @Column(unique = true)
    private String tokenVerificacion;

    private String tokenRecuperacion;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime ultimoAcceso;
    private boolean activo;

    public Usuario() {
        super();
        this.activo = true;
        this.emailVerificado = false;
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Crea un usuario con datos completos.
     *
     * @param nombre     nombre completo
     * @param email      correo electrónico
     * @param contrasena contraseña
     * @param rol        rol del usuario
     */
    public Usuario(String nombre, String email, String contrasena, RolUsuario rol) {
        super(nombre, email, contrasena);
        this.rol = rol;
        this.activo = true;
        this.emailVerificado = false;
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Retorna las autoridades del usuario según su rol.
     * Spring Security usa este método para la autorización.
     *
     * @return colección de autoridades del usuario
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    /**
     * Retorna la contraseña del usuario para Spring Security.
     *
     * @return contraseña del usuario
     */
    @Override
    public String getPassword() {
        return getContrasena();
    }

    /**
     * Retorna el email como identificador único del usuario para Spring Security.
     *
     * @return email del usuario
     */
    @Override
    public String getUsername() {
        return getEmail();
    }

    /**
     * Indica si la cuenta del usuario no ha expirado.
     *
     * @return siempre true ya que no manejamos expiración de cuenta
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica si la cuenta del usuario no está bloqueada.
     * Retorna true solo si el usuario está activo.
     *
     * @return true si el usuario está activo, false si está desactivado
     */
    @Override
    public boolean isAccountNonLocked() {
        return activo;
    }

    /**
     * Indica si las credenciales del usuario no han expirado.
     *
     * @return siempre true ya que no manejamos expiración de credenciales
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica si el usuario está habilitado en el sistema.
     * Retorna true solo si el usuario está activo y tiene el email verificado.
     *
     * @return true si el usuario está activo y verificado
     */
    @Override
    public boolean isEnabled() {
        return activo && emailVerificado;
    }

    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }
    public boolean isEmailVerificado() { return emailVerificado; }
    public void setEmailVerificado(boolean emailVerificado) { this.emailVerificado = emailVerificado; }
    public String getTokenVerificacion() { return tokenVerificacion; }
    public void setTokenVerificacion(String tokenVerificacion) { this.tokenVerificacion = tokenVerificacion; }
    public String getTokenRecuperacion() { return tokenRecuperacion; }
    public void setTokenRecuperacion(String tokenRecuperacion) { this.tokenRecuperacion = tokenRecuperacion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), activo, emailVerificado, fechaCreacion,
                fechaActualizacion, rol, tokenVerificacion, tokenRecuperacion);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        Usuario other = (Usuario) obj;
        return activo == other.activo && emailVerificado == other.emailVerificado
                && Objects.equals(fechaCreacion, other.fechaCreacion)
                && Objects.equals(fechaActualizacion, other.fechaActualizacion)
                && rol == other.rol
                && Objects.equals(tokenVerificacion, other.tokenVerificacion)
                && Objects.equals(tokenRecuperacion, other.tokenRecuperacion);
    }

    @Override
    public String toString() {
        return "Usuario [id=" + getId() + ", nombre=" + getNombre() + ", email=" + getEmail()
                + ", rol=" + rol + ", emailVerificado=" + emailVerificado
                + ", activo=" + activo + ", fechaCreacion=" + fechaCreacion + "]";
    }
}
