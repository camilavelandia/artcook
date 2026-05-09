package co.edu.unbosque.artcook.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auditorias")
public class Auditoria {
	
	private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) long id;
	private long usuarioId;
	private String accion;
	private String entidad;
	private Long idEntidad;
	private String detalles;
	private LocalDateTime fechaAccion;
	
	public Auditoria() {
		this.fechaAccion = LocalDateTime.now();
	}
	
	public Auditoria(long usuarioId, String accion, String entidad, Long idEntidad) {
		super();
		this.usuarioId = usuarioId;
		this.accion = accion;
		this.entidad = entidad;
		this.idEntidad = idEntidad;
		this.fechaAccion = LocalDateTime.now();
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getUsuarioId() {
		return usuarioId;
	}
	
	public void setUsuarioId(long usuarioId) {
		this.usuarioId = usuarioId;
	}
	
	public String getAccion() {
		return accion;
	}
	
	public void setAccion(String accion) {
		this.accion = accion;
	}
	
	public String getEntidad() {
		return entidad;
	}
	
	public void setEntidad(String entidad) {
		this.entidad = entidad;
	}
	
	public Long getIdEntidad() {
		return idEntidad;
	}
	
	public void setIdEntidad(Long idEntidad) {
		this.idEntidad = idEntidad;
	}
	
	public String getDetalles() {
		return detalles;
	}
	
	public void setDetalles(String detalles) {
		this.detalles = detalles;
	}
	
	public LocalDateTime getFechaAccion() {
		return fechaAccion;
	}
	
	public void setFechaAccion(LocalDateTime fechaAccion) {
		this.fechaAccion = fechaAccion;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(accion, detalles, entidad, fechaAccion, id, idEntidad, usuarioId);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Auditoria other = (Auditoria) obj;
		return Objects.equals(accion, other.accion) && Objects.equals(detalles, other.detalles)
				&& Objects.equals(entidad, other.entidad) && Objects.equals(fechaAccion, other.fechaAccion)
				&& id == other.id && Objects.equals(idEntidad, other.idEntidad) && usuarioId == other.usuarioId;
	}
	
	@Override
	public String toString() {
		return "Auditoria [id=" + id + ", usuarioId=" + usuarioId + ", accion=" + accion + ", entidad=" + entidad
				+ ", fechaAccion=" + fechaAccion + ", detalles=" + detalles + "]";
	}
}