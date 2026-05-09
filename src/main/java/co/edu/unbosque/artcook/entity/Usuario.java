package co.edu.unbosque.artcook.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario extends Persona {

	@Enumerated(EnumType.STRING)
	private RolUsuario rol;

	private boolean emailVerificado;
	private String tokenVerificacion;
	private String tokenRecuperacion;
	private LocalDateTime fechaCreacion;
	private LocalDateTime fechaActualizacion;
	private boolean activo;

	public Usuario() {
		super();
		this.activo = true;
		this.emailVerificado = false;
		this.fechaCreacion = LocalDateTime.now();
	}

	public Usuario(String nombre, String email, String contrasena, RolUsuario rol) {
		super(nombre, email, contrasena);
		this.rol = rol;
		this.activo = true;
		this.emailVerificado = false;
		this.fechaCreacion = LocalDateTime.now();
	}

	public RolUsuario getRol() {
		return rol;
	}

	public void setRol(RolUsuario rol) {
		this.rol = rol;
	}

	public boolean isEmailVerificado() {
		return emailVerificado;
	}

	public void setEmailVerificado(boolean emailVerificado) {
		this.emailVerificado = emailVerificado;
	}

	public String getTokenVerificacion() {
		return tokenVerificacion;
	}

	public void setTokenVerificacion(String tokenVerificacion) {
		this.tokenVerificacion = tokenVerificacion;
	}

	public String getTokenRecuperacion() {
		return tokenRecuperacion;
	}

	public void setTokenRecuperacion(String tokenRecuperacion) {
		this.tokenRecuperacion = tokenRecuperacion;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public LocalDateTime getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), activo, emailVerificado, fechaActualizacion, fechaCreacion, rol, tokenVerificacion, tokenRecuperacion);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return activo == other.activo && emailVerificado == other.emailVerificado
				&& Objects.equals(fechaActualizacion, other.fechaActualizacion)
				&& Objects.equals(fechaCreacion, other.fechaCreacion) && rol == other.rol
				&& Objects.equals(tokenVerificacion, other.tokenVerificacion)
				&& Objects.equals(tokenRecuperacion, other.tokenRecuperacion);
	}

	@Override
	public String toString() {
		return "Usuario [id=" + getId() + ", nombre=" + getNombre() + ", email=" + getEmail() + ", rol=" + rol
				+ ", emailVerificado=" + emailVerificado + ", activo=" + activo + ", fechaCreacion=" + fechaCreacion
				+ ", fechaActualizacion=" + fechaActualizacion + "]";
	}
}