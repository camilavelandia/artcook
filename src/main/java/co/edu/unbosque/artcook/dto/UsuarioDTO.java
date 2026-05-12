// clase modificada camila-juan

package co.edu.unbosque.artcook.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class UsuarioDTO {

	private long id;
	private String nombre;
	private String email;
	private String contrasena;
	private RolUsuarioDTO rol;
	private boolean emailVerificado;
	private LocalDateTime fechaCreacion;
	private boolean activo;

	public UsuarioDTO() {
	}

	public UsuarioDTO(String nombre, String email, String contrasena, RolUsuarioDTO rol) {
		super();
		this.nombre = nombre;
		this.email = email;
		this.contrasena = contrasena;
		this.rol = rol;
	}

	public long getId() { return id; }
	public void setId(long id) { this.id = id; }
	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getContrasena() { return contrasena; }
	public void setContrasena(String contrasena) { this.contrasena = contrasena; }
	public RolUsuarioDTO getRol() { return rol; }
	public void setRol(RolUsuarioDTO rol) { this.rol = rol; }
	public boolean isEmailVerificado() { return emailVerificado; }
	public void setEmailVerificado(boolean emailVerificado) { this.emailVerificado = emailVerificado; }
	public LocalDateTime getFechaCreacion() { return fechaCreacion; }
	public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
	public boolean isActivo() { return activo; }
	public void setActivo(boolean activo) { this.activo = activo; }

	@Override
	public int hashCode() {
		return Objects.hash(activo, email, emailVerificado, fechaCreacion, id, nombre, rol);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		UsuarioDTO other = (UsuarioDTO) obj;
		return activo == other.activo && Objects.equals(email, other.email)
				&& emailVerificado == other.emailVerificado
				&& Objects.equals(fechaCreacion, other.fechaCreacion)
				&& id == other.id && Objects.equals(nombre, other.nombre)
				&& rol == other.rol;
	}

	@Override
	public String toString() {
		return "UsuarioDTO [id=" + id + ", nombre=" + nombre + ", email=" + email
				+ ", rol=" + rol + ", emailVerificado=" + emailVerificado + ", activo=" + activo + "]";
	}
}
