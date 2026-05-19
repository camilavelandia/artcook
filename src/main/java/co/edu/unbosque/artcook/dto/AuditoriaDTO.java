package co.edu.unbosque.artcook.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Objeto de transferencia de datos para Auditoria.
 * Usa ID de usuario en lugar de email para mayor consistencia con el modelo de datos.
 */
public class AuditoriaDTO {

    private long id;
    private long usuarioId;
    private String accion;
    private String entidad;
    private Long idEntidad;
    private String detalles;
    private LocalDateTime fechaAccion;

    public AuditoriaDTO() {
    }

    /**
     * Crea un AuditoriaDTO con los datos principales del registro.
     *
     * @param usuarioId ID del usuario que realizó la acción
     * @param accion    acción realizada
     * @param entidad   entidad afectada
     * @param idEntidad ID del registro afectado
     */
    public AuditoriaDTO(long usuarioId, String accion, String entidad, Long idEntidad) {
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.entidad = entidad;
        this.idEntidad = idEntidad;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long usuarioId) { this.usuarioId = usuarioId; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public Long getIdEntidad() { return idEntidad; }
    public void setIdEntidad(Long idEntidad) { this.idEntidad = idEntidad; }
    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
    public LocalDateTime getFechaAccion() { return fechaAccion; }
    public void setFechaAccion(LocalDateTime fechaAccion) { this.fechaAccion = fechaAccion; }

    @Override
    public int hashCode() {
        return Objects.hash(accion, detalles, entidad, fechaAccion, id, idEntidad, usuarioId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AuditoriaDTO other = (AuditoriaDTO) obj;
        return Objects.equals(accion, other.accion) && Objects.equals(detalles, other.detalles)
                && Objects.equals(entidad, other.entidad)
                && Objects.equals(fechaAccion, other.fechaAccion)
                && id == other.id && Objects.equals(idEntidad, other.idEntidad)
                && usuarioId == other.usuarioId;
    }

    @Override
    public String toString() {
        return "AuditoriaDTO [id=" + id + ", usuarioId=" + usuarioId + ", accion=" + accion
                + ", entidad=" + entidad + ", fechaAccion=" + fechaAccion + ", detalles=" + detalles + "]";
    }
}
