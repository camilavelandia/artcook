// cambios en la clase

package co.edu.unbosque.artcook.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.artcook.dto.RecetaDTO;
import co.edu.unbosque.artcook.exception.RegistroNoEncontradoException;
import co.edu.unbosque.artcook.exception.TipoRecetaException;
import co.edu.unbosque.artcook.services.AuditoriaService;
import co.edu.unbosque.artcook.services.RecetaService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/receta")
@CrossOrigin(origins = { "http://localhost:8081", "*" })
public class RecetaController {

	@Autowired
	private RecetaService recetaSer;

	@Autowired
	private AuditoriaService auditoriaService;

	@PutMapping("/seleccionar")
	public ResponseEntity<String> seleccionarReceta(@RequestParam Long recetaId,
			@RequestParam String ia, @RequestParam long usuarioId,
			HttpServletRequest request) {

		if (!ia.equalsIgnoreCase("gpt") && !ia.equalsIgnoreCase("gemini")
				&& !ia.equalsIgnoreCase("claude")) {
			return new ResponseEntity<>("La IA debe ser gpt, gemini o claude.",
					HttpStatus.BAD_REQUEST);
		}

		int resultado = recetaSer.seleccionarReceta(recetaId, ia);

		switch (resultado) {
		case 0:
			auditoriaService.registrarAccion(usuarioId, "SELECCIONAR_RECETA", "Receta",
					recetaId, "Usuario selecciono receta de " + ia.toUpperCase());
			return new ResponseEntity<>("Receta seleccionada exitosamente.", HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("Receta no encontrada.", HttpStatus.NOT_FOUND);
		case 2:
			return new ResponseEntity<>("La IA seleccionada no tiene contenido disponible.",
					HttpStatus.BAD_REQUEST);
		default:
			return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/mostrartodo")
	public ResponseEntity<List<RecetaDTO>> obtenerTodo() {
		List<RecetaDTO> lista = recetaSer.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
	}

	@GetMapping("/porid")
	public ResponseEntity<RecetaDTO> obtenerPorId(@RequestParam Long id) {
		try {
			RecetaDTO receta = recetaSer.obtenerPorId(id);
			return new ResponseEntity<>(receta, HttpStatus.ACCEPTED);
		} catch (RegistroNoEncontradoException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/porusuario")
	public ResponseEntity<List<RecetaDTO>> obtenerPorUsuario(@RequestParam long usuarioId) {
		try {
			List<RecetaDTO> lista = recetaSer.obtenerPorUsuario(usuarioId);
			if (lista.isEmpty()) {
				return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		} catch (RegistroNoEncontradoException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/portipo")
	public ResponseEntity<List<RecetaDTO>> obtenerPorTipo(@RequestParam String tipo) {
		try {
			List<RecetaDTO> lista = recetaSer.obtenerPorTipo(tipo);
			if (lista.isEmpty()) {
				return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		} catch (TipoRecetaException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping("/actualizar")
	public ResponseEntity<String> actualizar(@RequestParam Long id,
			@RequestParam(required = false) String titulo,
			@RequestParam(required = false) String jsonRecetaSeleccionada,
			@RequestParam(required = false) String iaSeleccionada,
			@RequestParam(required = false) String urlVideo,
			@RequestParam(required = false) String urlAudio,
			@RequestParam(required = false) String guionNaracion,
			@RequestParam long usuarioId,
			HttpServletRequest request) {

		RecetaDTO nuevo = new RecetaDTO();
		nuevo.setTitulo(titulo);
		nuevo.setJsonRecetaSeleccionada(jsonRecetaSeleccionada);
		nuevo.setIaSeleccionada(iaSeleccionada);
		nuevo.setUrlVideo(urlVideo);
		nuevo.setUrlAudio(urlAudio);
		nuevo.setGuionNaracion(guionNaracion);

		int resultado;
		try {
			resultado = recetaSer.updateById(id, nuevo);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		switch (resultado) {
		case 0:
			auditoriaService.registrarAccion(usuarioId, "ACTUALIZAR_RECETA", "Receta",
					id, "Receta ID " + id + " actualizada");
			return new ResponseEntity<>("Receta actualizada exitosamente.", HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("Receta no encontrada.", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/desactivar")
	public ResponseEntity<String> desactivar(@RequestParam Long id,
			@RequestParam long usuarioId, HttpServletRequest request) {
		try {
			int resultado = recetaSer.desactivarReceta(id);
			if (resultado == 0) {
				auditoriaService.registrarAccion(usuarioId, "DESACTIVAR_RECETA", "Receta",
						id, "Receta ID " + id + " desactivada");
				return new ResponseEntity<>("Receta desactivada exitosamente.", HttpStatus.ACCEPTED);
			}
			return new ResponseEntity<>("Receta no encontrada.", HttpStatus.NOT_FOUND);
		} catch (RegistroNoEncontradoException e) {
			return new ResponseEntity<>("Receta no encontrada.", HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping("/eliminar")
	public ResponseEntity<String> eliminar(@RequestParam Long id,
			@RequestParam long usuarioId, HttpServletRequest request) {
		int resultado;
		try {
			resultado = recetaSer.deleteById(id);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		switch (resultado) {
		case 0:
			auditoriaService.registrarAccion(usuarioId, "ELIMINAR_RECETA", "Receta",
					id, "Receta ID " + id + " eliminada");
			return new ResponseEntity<>("Receta eliminada exitosamente.", HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("Receta no encontrada.", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/contar")
	public ResponseEntity<Long> contar() {
		return new ResponseEntity<>(recetaSer.count(), HttpStatus.OK);
	}

	@GetMapping("/existe")
	public ResponseEntity<Boolean> existe(@RequestParam Long id) {
		return new ResponseEntity<>(recetaSer.exist(id), HttpStatus.OK);
	}

	@GetMapping("/generarPdf")
	public ResponseEntity<byte[]> generarPdfReceta(@RequestParam Long idReceta,
			@RequestParam long usuarioId, HttpServletRequest request) {
		try {
			byte[] pdf = recetaSer.generarPdfReceta(idReceta);
			auditoriaService.registrarAccion(usuarioId, "GENERAR_PDF", "Receta",
					idReceta, "PDF generado para receta ID " + idReceta);
			return ResponseEntity.ok()
					.header("Content-Disposition", "attachment; filename=receta_" + idReceta + ".pdf")
					.header("Content-Type", "application/pdf")
					.body(pdf);
		} catch (RegistroNoEncontradoException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
