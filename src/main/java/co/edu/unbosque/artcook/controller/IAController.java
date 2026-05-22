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

	// Endpoint nuevo — permite al usuario elegir una sola IA para generar
	// el parametro 'ia' puede ser: gpt, gemini o claude
	@PostMapping("/generar")
	public ResponseEntity<?> generarConIASeleccionada(
			@RequestParam String prompt,
			@RequestParam String tipo,
			@RequestParam(required = false) Integer porciones,
			@RequestParam long usuarioId,
			@RequestParam String ia) {

		// Si no se envian porciones se usa 1 por defecto
		Integer porcionesFinales = porciones != null ? porciones : 1;

		try {
			// Llama al servicio correspondiente segun la IA elegida
			String resultado = switch (ia.toLowerCase()) {
				case "gpt"    -> iaService.generarRecetaConGPT(prompt, tipo, porcionesFinales);
				case "gemini" -> iaService.generarRecetaConGemini(prompt, tipo, porcionesFinales);
				case "claude" -> iaService.generarRecetaConClaude(prompt, tipo, porcionesFinales);
				default -> throw new IllegalArgumentException("IA no valida. Usa: gpt, gemini o claude");
			};

			auditoriaService.registrarAccion(usuarioId, "GENERAR_" + ia.toUpperCase(),
					"IA", null, "Generado con " + ia + ". Prompt: " + prompt);

			return new ResponseEntity<>(resultado, HttpStatus.OK);

		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (PromptVacioException e) {
			return new ResponseEntity<>("El prompt no puede estar vacio.", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado al consultar la IA.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/generar-todas")
	public ResponseEntity<?> generarConTodasLasIAs(@RequestParam String titulo, @RequestParam String tipo,
			@RequestParam String prompt, @RequestParam(required = false) Integer porciones,
			@RequestParam long usuarioId) {

		logGeminiKey();

		ResponseEntity<String> tipoValidation = validateTipo(tipo);
		if (tipoValidation != null) {
			return tipoValidation;
		}

		ResponseEntity<String> porcionesValidation = validatePorciones(tipo, porciones);
		if (porcionesValidation != null) {
			return porcionesValidation;
		}

		int porcionesFinales = obtenerPorcionesFinales(porciones);

		CompletableFuture<String> futuroGPT = crearFuturoGPT(prompt, tipo, porcionesFinales);
		CompletableFuture<String> futuroGemini = crearFuturoGemini(prompt, tipo, porcionesFinales);
		CompletableFuture<String> futuroClaude = crearFuturoClaude(prompt, tipo, porcionesFinales);

		CompletableFuture.allOf(futuroGPT, futuroGemini, futuroClaude).join();

		String resultado = combinarResultados(titulo, tipo, porcionesFinales,
			futuroGPT.join(), futuroGemini.join(), futuroClaude.join());

		auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_TODAS", "IA", null,
			"Receta generada con todas las IAs. Prompt: " + prompt);
		return new ResponseEntity<>(resultado, HttpStatus.OK);
	}

	private void logGeminiKey() {
		System.out.println("GEMINI KEY: " + iaService.getGeminiKey());
	}

	private ResponseEntity<String> validateTipo(String tipo) {
		if (!tipo.equalsIgnoreCase("COCINA") && !tipo.equalsIgnoreCase("MANUALIDAD")) {
			return new ResponseEntity<>("El tipo debe ser COCINA o MANUALIDAD.", HttpStatus.BAD_REQUEST);
		}
		return null;
	}

	private ResponseEntity<String> validatePorciones(String tipo, Integer porciones) {
		if (tipo.equalsIgnoreCase("COCINA") && (porciones == null || porciones <= 0)) {
			return new ResponseEntity<>("Las porciones deben ser mayor a 0.", HttpStatus.BAD_REQUEST);
		}
		return null;
	}

	private Integer obtenerPorcionesFinales(Integer porciones) {
		return porciones != null ? porciones : 1;
	}

	private CompletableFuture<String> crearFuturoGPT(String prompt, String tipo, Integer porciones) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return iaService.generarRecetaConGPT(prompt, tipo, porciones);
			} catch (Exception e) {
				System.out.println("ERROR GPT: " + e.getMessage());
				return null;
			}
		});
	}

	private CompletableFuture<String> crearFuturoGemini(String prompt, String tipo, Integer porciones) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return iaService.generarRecetaConGemini(prompt, tipo, porciones);
			} catch (Exception e) {
				System.out.println("ERROR GEMINI: " + e.getMessage());
				return null;
			}
		});
	}

	private CompletableFuture<String> crearFuturoClaude(String prompt, String tipo, Integer porciones) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return iaService.generarRecetaConClaude(prompt, tipo, porciones);
			} catch (Exception e) {
				System.out.println("ERROR CLAUDE: " + e.getMessage());
				return null;
			}
		});
	}

	private String combinarResultados(String titulo, String tipo, Integer porciones,
		String gpt, String gemini, String claude) {
		return "Título: " + titulo + "\nTipo: " + tipo + "\nPorciones: " + porciones
			+ "\n--- GPT ---\n" + gpt + "\n--- Gemini ---\n" + gemini + "\n--- Claude ---\n" + claude;
	}
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
