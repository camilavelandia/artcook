//clase agregada por camila

package co.edu.unbosque.artcook.dto;

import java.util.Objects;

public class LoginResponseDTO {

	private long id;
	private String nombre;
	private String email;
	private RolUsuarioDTO rol;
	private boolean emailVerificado;

	public LoginResponseDTO() {
	}

	public LoginResponseDTO(long id, String nombre, String email, RolUsuarioDTO rol, boolean emailVerificado) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.email = email;
		this.rol = rol;
		this.emailVerificado = emailVerificado;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public RolUsuarioDTO getRol() {
		return rol;
	}

	public void setRol(RolUsuarioDTO rol) {
		this.rol = rol;
	}

	public boolean isEmailVerificado() {
		return emailVerificado;
	}

	public void setEmailVerificado(boolean emailVerificado) {
		this.emailVerificado = emailVerificado;
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, emailVerificado, id, nombre, rol);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LoginResponseDTO other = (LoginResponseDTO) obj;
		return Objects.equals(email, other.email) && emailVerificado == other.emailVerificado
				&& id == other.id && Objects.equals(nombre, other.nombre) && rol == other.rol;
	}

	@Override
	public String toString() {
		return "LoginResponseDTO [id=" + id + ", nombre=" + nombre + ", email=" + email
				+ ", rol=" + rol + ", emailVerificado=" + emailVerificado + "]";
	}
}
