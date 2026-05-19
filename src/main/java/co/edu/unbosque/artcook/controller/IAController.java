package co.edu.unbosque.artcook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.artcook.dto.RecetaDTO;
import co.edu.unbosque.artcook.dto.TipoRecetaDTO;
import co.edu.unbosque.artcook.exception.CampoVacioException;
import co.edu.unbosque.artcook.exception.PromptVacioException;
import co.edu.unbosque.artcook.services.AuditoriaService;
import co.edu.unbosque.artcook.services.IAService;
import co.edu.unbosque.artcook.services.RecetaService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ia")
@CrossOrigin(origins = { "http://localhost:8081", "*" })
public class IAController {

	@Autowired
	private IAService iaService;

	@Autowired
	private AuditoriaService auditoriaService;

	@Autowired
	private RecetaService recetaSer;

	@PostMapping("/generar-gpt")
	public ResponseEntity<?> generarConGPT(@RequestParam String prompt,
			@RequestParam long usuarioId) {
		try {
			String resultado = iaService.generarRecetaConGPT(prompt);
			auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_GPT", "IA",
					null, "Receta generada con GPT. Prompt: " + prompt);
			return new ResponseEntity<>(resultado, HttpStatus.OK);
		} catch (PromptVacioException e) {
			return new ResponseEntity<>("El prompt no puede estar vacío o es muy corto.",
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado al consultar GPT.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/generar-gemini")
	public ResponseEntity<?> generarConGemini(@RequestParam String prompt,
			@RequestParam long usuarioId) {
		try {
			String resultado = iaService.generarRecetaConGemini(prompt);
			auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_GEMINI", "IA",
					null, "Receta generada con Gemini. Prompt: " + prompt);
			return new ResponseEntity<>(resultado, HttpStatus.OK);
		} catch (PromptVacioException e) {
			return new ResponseEntity<>("El prompt no puede estar vacío o es muy corto.",
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado al consultar Gemini.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/generar-claude")
	public ResponseEntity<?> generarConClaude(@RequestParam String prompt,
			@RequestParam long usuarioId) {
		try {
			String resultado = iaService.generarRecetaConClaude(prompt);
			auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_CLAUDE", "IA",
					null, "Receta generada con Claude. Prompt: " + prompt);
			return new ResponseEntity<>(resultado, HttpStatus.OK);
		} catch (PromptVacioException e) {
			return new ResponseEntity<>("El prompt no puede estar vacío o es muy corto.",
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado al consultar Claude.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/generar-todas")
	public ResponseEntity<?> generarConTodasLasIAs(
			@RequestParam String titulo,
			@RequestParam String tipo,
			@RequestParam String prompt,
			@RequestParam(required = false) Integer porciones,
			@RequestParam long usuarioId) {

		if (!tipo.equalsIgnoreCase("COCINA") && !tipo.equalsIgnoreCase("MANUALIDAD")) {
			return new ResponseEntity<>("El tipo debe ser COCINA o MANUALIDAD.", HttpStatus.BAD_REQUEST);
		}

		if (tipo.equalsIgnoreCase("COCINA") && (porciones == null || porciones <= 0)) {
			return new ResponseEntity<>("Las porciones deben ser mayor a 0 para recetas de cocina.",
					HttpStatus.BAD_REQUEST);
		}

		String resultadoGPT = null;
		String resultadoGemini = null;
		String resultadoClaude = null;
		String errorGPT = null;
		String errorGemini = null;
		String errorClaude = null;

		try {
			resultadoGPT = iaService.generarRecetaConGPT(prompt);
		} catch (PromptVacioException e) {
			return new ResponseEntity<>("El prompt no puede estar vacío o es muy corto.",
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			errorGPT = "No se pudo obtener respuesta de GPT.";
		}

		try {
			resultadoGemini = iaService.generarRecetaConGemini(prompt);
		} catch (PromptVacioException e) {
			return new ResponseEntity<>("El prompt no puede estar vacío o es muy corto.",
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			errorGemini = "No se pudo obtener respuesta de Gemini.";
		}

		try {
			resultadoClaude = iaService.generarRecetaConClaude(prompt);
		} catch (PromptVacioException e) {
			return new ResponseEntity<>("El prompt no puede estar vacío o es muy corto.",
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			errorClaude = "No se pudo obtener respuesta de Claude.";
		}

		TipoRecetaDTO tipoDTO = TipoRecetaDTO.valueOf(tipo.toUpperCase());
		RecetaDTO recetaDTO = new RecetaDTO(titulo, prompt, tipoDTO, porciones, usuarioId);
		recetaDTO.setJsonRecetaGPT(resultadoGPT);
		recetaDTO.setJsonRecetaGemini(resultadoGemini);
		recetaDTO.setJsonRecetaClaude(resultadoClaude);

		int resultadoGuardado = recetaSer.create(recetaDTO);
		if (resultadoGuardado != 0) {
			return new ResponseEntity<>("Error al guardar la receta en la base de datos.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_TODAS_IAS", "Receta",
				recetaDTO.getId(), "Receta generada con las 3 IAs. Prompt: " + prompt);

		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("recetaId", recetaDTO.getId());
		respuesta.put("prompt", prompt);
		respuesta.put("recetaGPT", resultadoGPT != null ? resultadoGPT : errorGPT);
		respuesta.put("recetaGemini", resultadoGemini != null ? resultadoGemini : errorGemini);
		respuesta.put("recetaClaude", resultadoClaude != null ? resultadoClaude : errorClaude);
		respuesta.put("exitosaGPT", resultadoGPT != null);
		respuesta.put("exitosaGemini", resultadoGemini != null);
		respuesta.put("exitosaClaude", resultadoClaude != null);

		return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
	}

	@PostMapping("/narracion")
	public ResponseEntity<?> generarNarracion(@RequestParam String textoReceta,
			@RequestParam long usuarioId) {
		try {
			String narracion = iaService.generarNarracionConIA(textoReceta);
			auditoriaService.registrarAccion(usuarioId, "GENERAR_NARRACION", "IA",
					null, "Narración generada para receta.");
			return new ResponseEntity<>(narracion, HttpStatus.OK);
		} catch (CampoVacioException e) {
			return new ResponseEntity<>("El texto de la receta no puede estar vacío.",
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado al generar la narración.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
