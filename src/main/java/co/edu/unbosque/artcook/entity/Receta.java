// clase modificada camila-juan

package co.edu.unbosque.artcook.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recetas")
public class Receta {

	private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) long id;
	private String titulo;
	private String promptOriginal;

	@Enumerated(EnumType.STRING)
	private TipoReceta tipoReceta;

	private Integer porciones;

	@Column(columnDefinition = "TEXT")
	private String jsonRecetaGPT;

	@Column(columnDefinition = "TEXT")
	private String jsonRecetaClaude;

	@Column(columnDefinition = "TEXT")
	private String jsonRecetaGemini;

	@Column(columnDefinition = "TEXT")
	private String jsonRecetaSeleccionada;

	private String iaSeleccionada;
	private String urlVideo;
	private String urlAudio;

	@Column(columnDefinition = "TEXT")
	private String guionNaracion;

	private long usuarioId; 

	private LocalDateTime fechaCreacion;
	private LocalDateTime fechaActualizacion;
	private boolean activa;

	public Receta() {
		this.fechaCreacion = LocalDateTime.now();
		this.activa = true;
	}

	public Receta(String titulo, String promptOriginal, TipoReceta tipoReceta, long usuarioId) {
		super();
		this.titulo = titulo;
		this.promptOriginal = promptOriginal;
		this.tipoReceta = tipoReceta;
		this.usuarioId = usuarioId;
		this.fechaCreacion = LocalDateTime.now();
		this.activa = true;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getPromptOriginal() {
		return promptOriginal;
	}

	public void setPromptOriginal(String promptOriginal) {
		this.promptOriginal = promptOriginal;
	}

	public TipoReceta getTipoReceta() {
		return tipoReceta;
	}

	public void setTipoReceta(TipoReceta tipoReceta) {
		this.tipoReceta = tipoReceta;
	}

	public Integer getPorciones() {
		return porciones;
	}

	public void setPorciones(Integer porciones) {
		this.porciones = porciones;
	}

	public String getJsonRecetaGPT() {
		return jsonRecetaGPT;
	}

	public void setJsonRecetaGPT(String jsonRecetaGPT) {
		this.jsonRecetaGPT = jsonRecetaGPT;
	}

	public String getJsonRecetaClaude() {
		return jsonRecetaClaude;
	}

	public void setJsonRecetaClaude(String jsonRecetaClaude) {
		this.jsonRecetaClaude = jsonRecetaClaude;
	}

	public String getJsonRecetaGemini() {
		return jsonRecetaGemini;
	}

	public void setJsonRecetaGemini(String jsonRecetaGemini) {
		this.jsonRecetaGemini = jsonRecetaGemini;
	}

	public String getJsonRecetaSeleccionada() {
		return jsonRecetaSeleccionada;
	}

	public void setJsonRecetaSeleccionada(String jsonRecetaSeleccionada) {
		this.jsonRecetaSeleccionada = jsonRecetaSeleccionada;
	}

	public String getIaSeleccionada() {
		return iaSeleccionada;
	}

	public void setIaSeleccionada(String iaSeleccionada) {
		this.iaSeleccionada = iaSeleccionada;
	}

	public String getUrlVideo() {
		return urlVideo;
	}

	public void setUrlVideo(String urlVideo) {
		this.urlVideo = urlVideo;
	}

	public String getUrlAudio() {
		return urlAudio;
	}

	public void setUrlAudio(String urlAudio) {
		this.urlAudio = urlAudio;
	}

	public String getGuionNaracion() {
		return guionNaracion;
	}

	public void setGuionNaracion(String guionNaracion) {
		this.guionNaracion = guionNaracion;
	}

	public long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(long usuarioId) {
		this.usuarioId = usuarioId;
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

	public boolean isActiva() {
		return activa;
	}

	public void setActiva(boolean activa) {
		this.activa = activa;
	}


	@Override
	public int hashCode() {
		return Objects.hash(activa, fechaActualizacion, fechaCreacion, guionNaracion, iaSeleccionada, id,
				jsonRecetaClaude, jsonRecetaGPT, jsonRecetaGemini, jsonRecetaSeleccionada, porciones, promptOriginal,
				tipoReceta, titulo, urlAudio, urlVideo, usuarioId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Receta other = (Receta) obj;
		return activa == other.activa && Objects.equals(fechaActualizacion, other.fechaActualizacion)
				&& Objects.equals(fechaCreacion, other.fechaCreacion)
				&& Objects.equals(guionNaracion, other.guionNaracion)
				&& Objects.equals(iaSeleccionada, other.iaSeleccionada) && id == other.id
				&& Objects.equals(jsonRecetaClaude, other.jsonRecetaClaude)
				&& Objects.equals(jsonRecetaGPT, other.jsonRecetaGPT)
				&& Objects.equals(jsonRecetaGemini, other.jsonRecetaGemini)
				&& Objects.equals(jsonRecetaSeleccionada, other.jsonRecetaSeleccionada)
				&& Objects.equals(porciones, other.porciones) && Objects.equals(promptOriginal, other.promptOriginal)
				&& tipoReceta == other.tipoReceta && Objects.equals(titulo, other.titulo)
				&& Objects.equals(urlAudio, other.urlAudio) && Objects.equals(urlVideo, other.urlVideo)
				&& usuarioId == other.usuarioId;
	}



	@Override
	public String toString() {
		return "Receta [id=" + id + ", titulo=" + titulo + ", tipoReceta=" + tipoReceta
				+ ", porciones=" + porciones + ", iaSeleccionada=" + iaSeleccionada
				+ ", urlVideo=" + urlVideo + ", usuarioId=" + usuarioId
				+ ", fechaCreacion=" + fechaCreacion + ", activa=" + activa + "]";
	}

	
}
