package co.edu.unbosque.artcook.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import co.edu.unbosque.artcook.exception.CampoVacioException;
import co.edu.unbosque.artcook.exception.LanzadorExcepciones;
import co.edu.unbosque.artcook.exception.PromptVacioException;

/**
 * Servicio que gestiona las llamadas a las tres IAs del sistema. Conecta con
 * OpenAI (GPT), Google (Gemini) y Anthropic (Claude).
 */
@Service
public class IAService {

	@Value("${app.openai.api-key:pending}")
	private String openAiApiKey;

	@Value("${app.gemini.api-key:pending}")
	private String geminiApiKey;

	@Value("${app.claude.api-key:pending}")
	private String claudeApiKey;

	private final RestTemplate restTemplate = crearRestTemplateConTimeout();

	/**
	 * Constructor por defecto.
	 */
	public IAService() {
	}

	private static RestTemplate crearRestTemplateConTimeout() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(10_000);
		factory.setReadTimeout(60_000);
		return new RestTemplate(factory);
	}

	/**
	 * Genera contenido usando OpenAI GPT con el prompt dado.
	 *
	 * @param prompt     prompt de la receta o manualidad
	 * @param tipoReceta tipo de receta (COCINA o MANUALIDAD)
	 * @param porciones  número de porciones
	 * @return contenido generado por GPT
	 * @throws PromptVacioException si el prompt es nulo, vacío o muy corto
	 */
	public String generarRecetaConGPT(String prompt, String tipoReceta, Integer porciones) throws PromptVacioException {
		LanzadorExcepciones.validarPrompt(prompt);
		if (openAiApiKey.equals("pending")) {
			return "OpenAI no esta configurado aun.";
		}
		try {
			String url = "https://api.openai.com/v1/chat/completions";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(openAiApiKey);

			Map<String, Object> body = Map.of("model", "gpt-4o-mini", "messages",
					List.of(Map.of("role", "user", "content", construirPromptGPT(prompt, tipoReceta, porciones))),
					"max_tokens", 1500);

			HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
			Map response = restTemplate.postForObject(url, request, Map.class);

			List choices = (List) response.get("choices");
			Map choice = (Map) choices.get(0);
			Map message = (Map) choice.get("message");
			return (String) message.get("content");

		} catch (Exception e) {
			return "Error al conectar con OpenAI: " + e.getMessage();
		}
	}

	/**
	 * Genera contenido usando Google Gemini con el prompt dado.
	 *
	 * @param prompt     prompt de la receta o manualidad
	 * @param tipoReceta tipo de receta (COCINA o MANUALIDAD)
	 * @param porciones  número de porciones
	 * @return contenido generado por Gemini
	 * @throws PromptVacioException si el prompt es nulo, vacío o muy corto
	 */
	public String generarRecetaConGemini(String prompt, String tipoReceta, Integer porciones)
			throws PromptVacioException {
		LanzadorExcepciones.validarPrompt(prompt);
		if (geminiApiKey.equals("pending")) {
			return "Gemini no esta configurado aun.";
		}

		String[] modelos = { "gemini-2.5-flash", "gemini-2.5-flash-lite" };

		for (String modelo : modelos) {
			try {
				System.out.println("GEMINI - Intentando con modelo: " + modelo);
				String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelo + ":generateContent";

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.set("x-goog-api-key", geminiApiKey);

				Map<String, Object> body = Map.of("contents",
						List.of(Map.of("parts",
								List.of(Map.of("text", construirPromptGemini(prompt, tipoReceta, porciones))))),
						"generationConfig", Map.of("maxOutputTokens", 1500, "temperature", 0.7));

				HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
				Map response = restTemplate.postForObject(url, request, Map.class);

				List candidates = (List) response.get("candidates");
				Map candidate = (Map) candidates.get(0);
				Map content = (Map) candidate.get("content");
				List parts = (List) content.get("parts");
				Map part = (Map) parts.get(0);
				String resultado = (String) part.get("text");
				System.out.println("GEMINI OK con modelo: " + modelo);
				return resultado;

			} catch (org.springframework.web.client.HttpStatusCodeException e) {
				int codigo = e.getStatusCode().value();
				System.out.println("GEMINI fallo con " + modelo + " - HTTP " + codigo);
				if (codigo == 429 || codigo == 503) {
					continue;
				}
				return "Error al conectar con Gemini por superar limite gratuito: " + e.getMessage();

			} catch (Exception e) {
				System.out.println("GEMINI error inesperado con " + modelo + ": " + e.getMessage());
				return "Error al conectar con Gemini: " + e.getMessage();
			}
		}

		return "Gemini no esta disponible en este momento. Intenta de nuevo en unos minutos.";
	}

	/**
	 * Genera contenido usando Anthropic Claude con el prompt dado.
	 *
	 * @param prompt     prompt de la receta o manualidad
	 * @param tipoReceta tipo de receta (COCINA o MANUALIDAD)
	 * @param porciones  número de porciones
	 * @return contenido generado por Claude
	 * @throws PromptVacioException si el prompt es nulo, vacío o muy corto
	 */
	public String generarRecetaConClaude(String prompt, String tipoReceta, Integer porciones)
			throws PromptVacioException {
		LanzadorExcepciones.validarPrompt(prompt);
		if (claudeApiKey.equals("pending")) {
			return "Claude no esta configurado aun.";
		}
		try {
			String url = "https://api.anthropic.com/v1/messages";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("x-api-key", claudeApiKey);
			headers.set("anthropic-version", "2023-06-01");

			Map<String, Object> body = Map.of("model", "claude-haiku-4-5", "max_tokens", 1500, "messages",
					List.of(Map.of("role", "user", "content", construirPromptClaude(prompt, tipoReceta, porciones))));

			HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
			Map response = restTemplate.postForObject(url, request, Map.class);

			List content = (List) response.get("content");
			Map block = (Map) content.get(0);
			return (String) block.get("text");

		} catch (org.springframework.web.client.HttpStatusCodeException e) {
			int codigo = e.getStatusCode().value();
			if (codigo == 529 || codigo == 503 || codigo == 429) {
				return "Claude no esta disponible en este momento. Intenta de nuevo en unos minutos.";
			}
			return "Error al conectar con Claude: " + e.getMessage();

		} catch (Exception e) {
			return "Error al conectar con Claude: " + e.getMessage();
		}
	}

	/**
	 * Genera una narración a partir del texto de una receta seleccionada.
	 *
	 * @param textoReceta texto de la receta para generar la narración
	 * @return narración generada
	 * @throws CampoVacioException si el texto de la receta está vacío
	 */
	public String generarNarracionConIA(String textoReceta) throws CampoVacioException {
		LanzadorExcepciones.validarCampoVacio(textoReceta, "textoReceta");
		try {
			return generarRecetaConGPT("Genera una narración clara y amigable para esta receta: " + textoReceta,
					"COCINA", 1);
		} catch (PromptVacioException e) {
			return "No se pudo generar la narracion.";
		}
	}

	/**
	 * Verifica si todas las IAs están configuradas con sus API keys.
	 *
	 * @return true si todas las IAs tienen API key configurada
	 */
	public boolean verificarConexion() {
		return !openAiApiKey.equals("pending") && !geminiApiKey.equals("pending") && !claudeApiKey.equals("pending");
	}

	/**
	 * Retorna la key de Gemini para diagnóstico.
	 *
	 * @return key de Gemini
	 */
	public String getGeminiKey() {
		return geminiApiKey;
	}

	/**
	 * Construye el prompt para OpenAI GPT — chef profesional de gastronomía
	 * internacional.
	 */
	private String construirPromptGPT(String prompt, String tipoReceta, Integer porciones) {
		return "Eres un chef profesional. El usuario quiere una " + tipoReceta + ". "
				+ "Trata siempre de interpretar su solicitud como una receta o manualidad válida. "
				+ "Solo responde con el error JSON si la solicitud es completamente absurda "
				+ "(ej: 'el cielo es azul'). "
				+ "Si es RECETA DE COCINA responde en JSON con: tipo, nombre, porciones, "
				+ "tiempo_preparacion, dificultad, ingredientes con cantidades exactas para " + porciones
				+ " personas, pasos detallados y consejos profesionales. "
				+ "Responde siempre en español.\n\nEl usuario pide: " + prompt;
	}

	/**
	 * Construye el prompt para Claude — nutricionista chef especializado en cocina
	 * saludable.
	 */
	private String construirPromptClaude(String prompt, String tipoReceta, Integer porciones) {
		return "Eres un asistente experto en recetas de cocina y manualidades. "
				+ "Detecta si el usuario pide una RECETA, una MANUALIDAD o algo INVALIDO. "
				+ "Responde SOLO con JSON valido, sin markdown ni explicaciones. "
				+ "Si es invalido responde EXACTAMENTE: "
				+ "{\"error\":\"Lo que escribiste no corresponde a una receta de cocina ni a una manualidad valida. Por favor intenta de nuevo.\"}. "
				+ "Si es RECETA responde con: tipo, nombre, porciones, tiempo_preparacion, calorias_por_porcion, "
				+ "ingredientes (array con nombre y cantidad ajustado para " + porciones + " personas), "
				+ "pasos (array de objetos con descripcion y tecnica, maximo 6 pasos breves). "
				+ "Si es MANUALIDAD responde con: tipo, nombre, dificultad, tiempo_estimado, "
				+ "materiales (array con nombre y cantidad), "
				+ "pasos (array de objetos con descripcion, maximo 6 pasos breves). " + "Mensaje del usuario: "
				+ prompt;
	}

	/**
	 * Construye el prompt para Gemini — chef casero experto en recetas rápidas y
	 * económicas.
	 */
	private String construirPromptGemini(String prompt, String tipoReceta, Integer porciones) {
		return "Eres un chef casero experto en recetas rapidas y economicas, y un experto en manualidades con materiales del hogar. "
				+ "Primero detecta si el usuario pide una RECETA DE COCINA, una MANUALIDAD, o algo invalido. "
				+ "Si no tiene sentido responde SOLO con: {\"error\": \"Lo que escribiste no corresponde a una receta de cocina ni a una manualidad valida. Por favor intenta de nuevo.\"}. "
				+ "Si es RECETA DE COCINA responde en JSON con: tipo, nombre, porciones, tiempo_preparacion, costo_aproximado, ingredientes faciles de conseguir con cantidades para "
				+ porciones + " personas, pasos simplificados y sustituciones. "
				+ "Si es MANUALIDAD responde en JSON con: tipo, nombre, dificultad, tiempo_estimado, materiales del hogar o economicos con cantidades, pasos simples y directos, y sustituciones_materiales. "
				+ "Ajusta siempre las cantidades segun el numero de personas. Responde siempre en espanol.\n\n"
				+ "El usuario pide: " + prompt;
	}
}