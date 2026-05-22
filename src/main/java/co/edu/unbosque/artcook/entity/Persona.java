package co.edu.unbosque.artcook.entity;

import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Clase base para las personas del sistema.
 * Contiene información común como nombre, email y contraseña.
 * El ID se define aquí para que sea heredado por las subclases.
 */
@MappedSuperclass
public abstract class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nombre;

    @Column(unique = true)
    private String email;

    private String contrasena;

    protected Persona() {
    }

    /**
     * Crea una persona con nombre, email y contraseña.
     *
     * @param nombre     nombre completo
     * @param email      correo electrónico
     * @param contrasena contraseña del usuario
     */
    protected Persona(String nombre, String email, String contrasena) {
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, email, contrasena);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Persona other = (Persona) obj;
        return id == other.id && Objects.equals(email, other.email);
    }

    @Override
    public String toString() {
        return "Persona [id=" + id + ", nombre=" + nombre + ", email=" + email + "]";
    }
}