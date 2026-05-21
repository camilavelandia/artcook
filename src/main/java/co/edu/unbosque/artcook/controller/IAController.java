package co.edu.unbosque.artcook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ia")
public class IAController {

	@Autowired
	private IAService iaService;

	@Autowired
	private AuditoriaService auditoriaService;

	@Autowired
	private RecetaService recetaSer;

	@PostMapping("/generar-gpt")
	public ResponseEntity<?> generarConGPT(@RequestParam String prompt, @RequestParam long usuarioId) {
		try {
			String resultado = iaService.generarRecetaConGPT(prompt, "COCINA", 2);
			auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_GPT", "IA", null,
					"Receta generada con GPT. Prompt: " + prompt);
			return new ResponseEntity<>(resultado, HttpStatus.OK);
		} catch (PromptVacioException e) {
			return new ResponseEntity<>("El prompt no puede estar vacío o es muy corto.", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado al consultar GPT.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/generar-gemini")
	public ResponseEntity<?> generarConGemini(@RequestParam String prompt, @RequestParam long usuarioId) {
		try {
			String resultado = iaService.generarRecetaConGemini(prompt, "COCINA", 2);
			auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_GEMINI", "IA", null,
					"Receta generada con Gemini. Prompt: " + prompt);
			return new ResponseEntity<>(resultado, HttpStatus.OK);
		} catch (PromptVacioException e) {
			return new ResponseEntity<>("El prompt no puede estar vacío o es muy corto.", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado al consultar Gemini.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/generar-claude")
	public ResponseEntity<?> generarConClaude(@RequestParam String prompt, @RequestParam long usuarioId) {
		try {
			String resultado = iaService.generarRecetaConClaude(prompt, "COCINA", 2);
			auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_CLAUDE", "IA", null,
					"Receta generada con Claude. Prompt: " + prompt);
			return new ResponseEntity<>(resultado, HttpStatus.OK);
		} catch (PromptVacioException e) {
			return new ResponseEntity<>("El prompt no puede estar vacío o es muy corto.", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado al consultar Claude.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/generar-todas")
	public ResponseEntity<?> generarConTodasLasIAs(@RequestParam String titulo, @RequestParam String tipo,
			@RequestParam String prompt, @RequestParam(required = false) Integer porciones,
			@RequestParam long usuarioId) {

		System.out.println("GEMINI KEY: " + iaService.getGeminiKey());

		if (!tipo.equalsIgnoreCase("COCINA") && !tipo.equalsIgnoreCase("MANUALIDAD")) {
			return new ResponseEntity<>("El tipo debe ser COCINA o MANUALIDAD.", HttpStatus.BAD_REQUEST);
		}
		if (tipo.equalsIgnoreCase("COCINA") && (porciones == null || porciones <= 0)) {
			return new ResponseEntity<>("Las porciones deben ser mayor a 0.", HttpStatus.BAD_REQUEST);
		}

		final Integer porcionesFinales = porciones != null ? porciones : 1;

		CompletableFuture<String> futuroGPT = CompletableFuture.supplyAsync(() -> {
			try {
				return iaService.generarRecetaConGPT(prompt, tipo, porcionesFinales);
			} catch (Exception e) {
				System.out.println("ERROR GPT: " + e.getMessage());
				return null;
			}
		});

		CompletableFuture<String> futuroGemini = CompletableFuture.supplyAsync(() -> {
			try {
				return iaService.generarRecetaConGemini(prompt, tipo, porcionesFinales);
			} catch (Exception e) {
				System.out.println("ERROR GEMINI: " + e.getMessage());
				return null;
			}
		});

		CompletableFuture<String> futuroClaude = CompletableFuture.supplyAsync(() -> {
			try {
				return iaService.generarRecetaConClaude(prompt, tipo, porcionesFinales);
			} catch (Exception e) {
				System.out.println("ERROR CLAUDE: " + e.getMessage());
				return null;
			}
		});

		try {
			CompletableFuture.allOf(futuroGPT, futuroGemini, futuroClaude).get(30, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			System.out.println("Timeout - tomando lo que llegó");
		} catch (Exception e) {
			System.out.println("Error en allOf: " + e.getMessage());
		}

		String resultadoGPT = futuroGPT.getNow("OpenAI no disponible.");
		String resultadoGemini = futuroGemini.getNow("Gemini no disponible.");
		String resultadoClaude = futuroClaude.getNow("Claude no disponible.");

		System.out.println("GPT: " + resultadoGPT);
		System.out.println("GEMINI: " + resultadoGemini);
		System.out.println("CLAUDE: " + resultadoClaude);

		TipoRecetaDTO tipoDTO = TipoRecetaDTO.valueOf(tipo.toUpperCase());
		RecetaDTO recetaDTO = new RecetaDTO(titulo, prompt, tipoDTO, porcionesFinales, usuarioId);
		recetaDTO.setJsonRecetaGPT(resultadoGPT);
		recetaDTO.setJsonRecetaGemini(resultadoGemini);
		recetaDTO.setJsonRecetaClaude(resultadoClaude);

		int resultadoGuardado = recetaSer.create(recetaDTO);
		if (resultadoGuardado != 0) {
			return new ResponseEntity<>("Error al guardar la receta.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_TODAS_IAS", "Receta", recetaDTO.getId(),
				"Receta generada con las 3 IAs. Prompt: " + prompt);

		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("recetaId", recetaDTO.getId());
		respuesta.put("prompt", prompt);
		respuesta.put("recetaGPT", resultadoGPT);
		respuesta.put("recetaGemini", resultadoGemini);
		respuesta.put("recetaClaude", resultadoClaude);
		respuesta.put("exitosaGPT", resultadoGPT != null && !resultadoGPT.equals("OpenAI no disponible."));
		respuesta.put("exitosaGemini", resultadoGemini != null && !resultadoGemini.equals("Gemini no disponible."));
		respuesta.put("exitosaClaude", resultadoClaude != null && !resultadoClaude.equals("Claude no disponible."));

		return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
	}

	@PostMapping("/narracion")
	public ResponseEntity<?> generarNarracion(@RequestParam String textoReceta, @RequestParam long usuarioId) {
		try {
			String narracion = iaService.generarNarracionConIA(textoReceta);
			auditoriaService.registrarAccion(usuarioId, "GENERAR_NARRACION", "IA", null,
					"Narración generada para receta.");
			return new ResponseEntity<>(narracion, HttpStatus.OK);
		} catch (CampoVacioException e) {
			return new ResponseEntity<>("El texto de la receta no puede estar vacío.", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado al generar la narración.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}