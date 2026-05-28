package co.edu.unbosque.artcook.services;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * Servicio encargado de gestionar las llamadas a las inteligencias artificiales
 * usadas por la aplicacion.
 *
 * <p>
 * Este servicio se conecta con OpenAI, Gemini y Claude para generar recetas de
 * cocina, manualidades y narraciones. Las API keys se cargan desde
 * {@code application.properties} mediante {@link Value}, por lo que no deben
 * escribirse directamente en esta clase.
 * </p>
 *
 * <p>
 * Tambien construye prompts estrictos para evitar que una receta sea generada en
 * el campo de manualidades o que una manualidad sea generada en el campo de
 * recetas.
 * </p>
 */
@Service
public class IAService {

	/**
	 * API key de OpenAI cargada desde application.properties.
	 */
	@Value("${app.openai.api-key}")
	private String openAiApiKey;

	/**
	 * API key de Gemini cargada desde application.properties.
	 */
	@Value("${app.gemini.api-key}")
	private String geminiApiKey;

	/**
	 * API key de Claude cargada desde application.properties.
	 */
	@Value("${app.claude.api-key}")
	private String claudeApiKey;

	/**
	 * Cliente HTTP usado para realizar las peticiones a las APIs externas.
	 */
	private final RestTemplate restTemplate = crearRestTemplateConTimeout();

	/**
	 * Constructor por defecto del servicio.
	 */
	public IAService() {
	}

	/**
	 * Crea un {@link RestTemplate} con tiempos maximos de conexion y lectura.
	 *
	 * @return instancia de RestTemplate configurada con timeout
	 */
	private static RestTemplate crearRestTemplateConTimeout() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(10_000);
		factory.setReadTimeout(60_000);
		return new RestTemplate(factory);
	}

	/**
	 * Verifica si una API key esta vacia, nula o sin configurar.
	 *
	 * @param apiKey API key a validar
	 * @return true si la API key no esta configurada, false en caso contrario
	 */
	private boolean apiKeyNoConfigurada(String apiKey) {
		return apiKey == null || apiKey.isBlank();
	}

	/**
	 * Determina si el tipo recibido corresponde a una manualidad.
	 *
	 * @param tipoReceta tipo enviado desde el formulario
	 * @return true si el tipo es MANUALIDAD, false en caso contrario
	 */
	private boolean esManualidad(String tipoReceta) {
		return tipoReceta != null && tipoReceta.equalsIgnoreCase("MANUALIDAD");
	}

	/**
	 * Obtiene la categoria esperada segun el tipo seleccionado por el usuario.
	 *
	 * @param tipoReceta tipo enviado desde el formulario
	 * @return MANUALIDAD si el campo es de manualidades, RECETA en caso contrario
	 */
	private String categoriaEsperada(String tipoReceta) {
		return esManualidad(tipoReceta) ? "MANUALIDAD" : "RECETA";
	}

	/**
	 * Construye el mensaje de error cuando el usuario escribe contenido en el
	 * campo equivocado.
	 *
	 * @param tipoReceta tipo enviado desde el formulario
	 * @return mensaje de error segun la categoria esperada
	 */
	private String mensajeErrorCategoria(String tipoReceta) {
		if (esManualidad(tipoReceta)) {
			return "Este campo solo recibe manualidades. No escribas recetas de cocina aqui. Por favor intenta de nuevo.";
		}
		return "Este campo solo recibe recetas de cocina. No escribas manualidades aqui. Por favor intenta de nuevo.";
	}

	/**
	 * Genera una receta o manualidad usando OpenAI GPT.
	 *
	 * @param prompt descripcion escrita por el usuario
	 * @param tipoReceta tipo de contenido esperado, RECETA o MANUALIDAD
	 * @param porciones numero de porciones para recetas de cocina
	 * @return respuesta generada por OpenAI en formato JSON o mensaje de error
	 * @throws PromptVacioException si el prompt esta vacio o no cumple la validacion
	 */
	public String generarRecetaConGPT(String prompt, String tipoReceta, Integer porciones) throws PromptVacioException {
		LanzadorExcepciones.validarPrompt(prompt);

		if (apiKeyNoConfigurada(openAiApiKey)) {
			return "OpenAI no esta configurado aun.";
		}

		try {
			String url = "https://api.openai.com/v1/chat/completions";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(openAiApiKey);

			Map<String, Object> body = Map.of(
					"model", "gpt-4o-mini",
					"messages", List.of(Map.of("role", "user", "content", construirPromptGPT(prompt, tipoReceta, porciones))),
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
	 * Genera una receta o manualidad usando Google Gemini.
	 *
	 * <p>
	 * Primero intenta usar {@code gemini-2.5-flash}. Si falla por cuota o
	 * disponibilidad, intenta con {@code gemini-2.5-flash-lite}.
	 * </p>
	 *
	 * <p>
	 * El prompt obliga a Gemini a respetar el campo actual: recetas solo en el
	 * campo de recetas y manualidades solo en el campo de manualidades.
	 * </p>
	 *
	 * @param prompt descripcion escrita por el usuario
	 * @param tipoReceta tipo de contenido esperado, RECETA o MANUALIDAD
	 * @param porciones numero de porciones para recetas de cocina
	 * @return respuesta generada por Gemini en formato JSON o mensaje de error
	 * @throws PromptVacioException si el prompt esta vacio o no cumple la validacion
	 */
	public String generarRecetaConGemini(String prompt, String tipoReceta, Integer porciones)
			throws PromptVacioException {
		LanzadorExcepciones.validarPrompt(prompt);

		if (apiKeyNoConfigurada(geminiApiKey)) {
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

				Map<String, Object> body = Map.of(
						"contents", List.of(Map.of("parts", List.of(Map.of("text", construirPromptGemini(prompt, tipoReceta, porciones))))),
						"generationConfig", Map.of(
								"maxOutputTokens", 2000,
								"temperature", 0.2,
								"thinkingConfig", Map.of("thinkingBudget", 0)));

				HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
				Map response = restTemplate.postForObject(url, request, Map.class);

				List candidates = (List) response.get("candidates");
				Map candidate = (Map) candidates.get(0);
				Map content = (Map) candidate.get("content");
				List parts = (List) content.get("parts");

				String resultado = null;
				for (Object partObj : parts) {
					Map partMap = (Map) partObj;
					Object isThought = partMap.get("thought");
					if (isThought == null || Boolean.FALSE.equals(isThought)) {
						resultado = (String) partMap.get("text");
					}
				}

				if (resultado == null) {
					Map ultimaParte = (Map) parts.get(parts.size() - 1);
					resultado = (String) ultimaParte.get("text");
				}

				resultado = resultado
						.replaceAll("(?i)```json", "")
						.replaceAll("```", "")
						.trim();

				Matcher matcher = Pattern.compile("\\{[\\s\\S]*\\}").matcher(resultado);
				if (matcher.find()) {
					resultado = matcher.group();
				}

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
	 * Genera una receta o manualidad usando Anthropic Claude.
	 *
	 * @param prompt descripcion escrita por el usuario
	 * @param tipoReceta tipo de contenido esperado, RECETA o MANUALIDAD
	 * @param porciones numero de porciones para recetas de cocina
	 * @return respuesta generada por Claude en formato JSON o mensaje de error
	 * @throws PromptVacioException si el prompt esta vacio o no cumple la validacion
	 */
	public String generarRecetaConClaude(String prompt, String tipoReceta, Integer porciones)
			throws PromptVacioException {
		LanzadorExcepciones.validarPrompt(prompt);

		if (apiKeyNoConfigurada(claudeApiKey)) {
			return "Claude no esta configurado aun.";
		}

		try {
			String url = "https://api.anthropic.com/v1/messages";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("x-api-key", claudeApiKey);
			headers.set("anthropic-version", "2023-06-01");

			Map<String, Object> body = Map.of(
					"model", "claude-haiku-4-5",
					"max_tokens", 1500,
					"messages", List.of(Map.of("role", "user", "content", construirPromptClaude(prompt, tipoReceta, porciones))));

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
	 * Genera una narracion clara y amigable a partir del texto de una receta.
	 *
	 * @param textoReceta texto base de la receta
	 * @return narracion generada o mensaje de error
	 * @throws CampoVacioException si el texto de la receta esta vacio
	 */
	public String generarNarracionConIA(String textoReceta) throws CampoVacioException {
		LanzadorExcepciones.validarCampoVacio(textoReceta, "textoReceta");

		try {
			return generarRecetaConGPT("Genera una narracion clara y amigable para esta receta: " + textoReceta, "COCINA", 1);
		} catch (PromptVacioException e) {
			return "No se pudo generar la narracion.";
		}
	}

	/**
	 * Verifica si las tres API keys estan configuradas.
	 *
	 * @return true si OpenAI, Gemini y Claude tienen API key, false en caso contrario
	 */
	public boolean verificarConexion() {
		return !apiKeyNoConfigurada(openAiApiKey)
				&& !apiKeyNoConfigurada(geminiApiKey)
				&& !apiKeyNoConfigurada(claudeApiKey);
	}

	/**
	 * Retorna un mensaje de diagnostico sobre Gemini sin exponer la API key real.
	 *
	 * @return estado de configuracion de Gemini
	 */
	public String getGeminiKey() {
		return apiKeyNoConfigurada(geminiApiKey) ? "Gemini no configurado" : "Gemini configurado";
	}

	/**
	 * Construye el prompt para OpenAI GPT segun la categoria esperada.
	 *
	 * @param prompt descripcion escrita por el usuario
	 * @param tipoReceta tipo de contenido esperado
	 * @param porciones numero de porciones para recetas
	 * @return prompt final para OpenAI
	 */
	private String construirPromptGPT(String prompt, String tipoReceta, Integer porciones) {
		String categoria = categoriaEsperada(tipoReceta);
		String error = mensajeErrorCategoria(tipoReceta);

		if ("MANUALIDAD".equals(categoria)) {
			return "Eres un experto en manualidades creativas. "
					+ "Este campo es EXCLUSIVAMENTE para MANUALIDADES. "
					+ "Antes de responder, verifica estrictamente si la solicitud del usuario corresponde a una manualidad. "
					+ "Una manualidad es un objeto, decoracion, arte, arreglo o proyecto hecho con materiales y pasos manuales. "
					+ "NO aceptes recetas de cocina, comidas, bebidas, postres, preparaciones comestibles ni instrucciones para cocinar. "
					+ "Si el usuario pide una receta de cocina o algo que no sea una manualidad, responde EXACTAMENTE: "
					+ "{\"error\":\"" + error + "\"}. "
					+ "Si es una manualidad valida, responde SOLO en JSON con esta estructura: "
					+ "{\"tipo\":\"MANUALIDAD\",\"nombre\":\"...\",\"dificultad\":\"...\",\"tiempo_estimado\":\"...\","
					+ "\"materiales\":[{\"nombre\":\"...\",\"cantidad\":\"...\"}],"
					+ "\"pasos\":[\"paso 1 en una sola frase\",\"paso 2 en una sola frase\"]}. "
					+ "El campo tipo DEBE ser exactamente MANUALIDAD. "
					+ "Maximo 5 pasos. Responde en espanol, solo JSON, sin markdown.\n\n"
					+ "Solicitud del usuario: " + prompt;
		}

		return "Eres un chef experto en recetas de cocina. "
				+ "Este campo es EXCLUSIVAMENTE para RECETAS DE COCINA. "
				+ "Antes de responder, verifica estrictamente si la solicitud del usuario corresponde a una receta de cocina. "
				+ "Una receta de cocina es una preparacion comestible o bebida con ingredientes y pasos de preparacion. "
				+ "NO aceptes manualidades, decoraciones, objetos, arreglos, proyectos artisticos ni trabajos con materiales no comestibles. "
				+ "Si el usuario pide una manualidad o algo que no sea una receta de cocina, responde EXACTAMENTE: "
				+ "{\"error\":\"" + error + "\"}. "
				+ "Si es una receta valida, responde SOLO en JSON con esta estructura: "
				+ "{\"tipo\":\"RECETA\",\"nombre\":\"...\",\"porciones\":" + porciones + ",\"tiempo_preparacion\":\"...\",\"dificultad\":\"...\","
				+ "\"ingredientes\":[{\"nombre\":\"...\",\"cantidad\":\"cantidad para " + porciones + " personas\"}],"
				+ "\"pasos\":[\"paso 1 en una sola frase\",\"paso 2 en una sola frase\"]}. "
				+ "El campo tipo DEBE ser exactamente RECETA. "
				+ "El campo porciones DEBE ser exactamente " + porciones + ". "
				+ "Las cantidades de ingredientes deben estar calculadas para " + porciones + " personas. "
				+ "Maximo 5 pasos. Responde en espanol, solo JSON, sin markdown.\n\n"
				+ "Solicitud del usuario: " + prompt;
	}

	/**
	 * Construye el prompt para Anthropic Claude segun la categoria esperada.
	 *
	 * @param prompt descripcion escrita por el usuario
	 * @param tipoReceta tipo de contenido esperado
	 * @param porciones numero de porciones para recetas
	 * @return prompt final para Claude
	 */
	private String construirPromptClaude(String prompt, String tipoReceta, Integer porciones) {
		String categoria = categoriaEsperada(tipoReceta);
		String error = mensajeErrorCategoria(tipoReceta);

		if ("MANUALIDAD".equals(categoria)) {
			return "Eres un asistente experto UNICAMENTE en manualidades. "
					+ "El campo actual solo permite MANUALIDADES. "
					+ "Debes rechazar cualquier receta de cocina, comida, bebida, postre o preparacion comestible. "
					+ "Si la solicitud no es una manualidad valida, responde EXACTAMENTE: "
					+ "{\"error\":\"" + error + "\"}. "
					+ "Si es una manualidad valida, responde EXACTAMENTE con esta estructura JSON: "
					+ "{\"tipo\":\"MANUALIDAD\",\"nombre\":\"...\",\"dificultad\":\"...\",\"tiempo_estimado\":\"...\","
					+ "\"materiales\":[{\"nombre\":\"...\",\"cantidad\":\"...\"}],"
					+ "\"pasos\":[\"paso 1 en una sola frase\",\"paso 2 en una sola frase\"]}. "
					+ "REGLAS OBLIGATORIAS: "
					+ "1. El campo tipo DEBE ser exactamente MANUALIDAD. "
					+ "2. No incluyas ingredientes ni porciones. "
					+ "3. No generes recetas de cocina bajo ninguna circunstancia. "
					+ "4. Los pasos deben ser strings simples, NO objetos. Maximo 5 pasos. "
					+ "Responde SOLO con JSON valido, sin markdown ni texto adicional. "
					+ "Mensaje del usuario: " + prompt;
		}

		return "Eres un asistente experto UNICAMENTE en recetas de cocina. "
				+ "El campo actual solo permite RECETAS DE COCINA. "
				+ "Debes rechazar cualquier manualidad, decoracion, objeto, arreglo, arte o proyecto con materiales no comestibles. "
				+ "Si la solicitud no es una receta de cocina valida, responde EXACTAMENTE: "
				+ "{\"error\":\"" + error + "\"}. "
				+ "Si es una receta valida, responde EXACTAMENTE con esta estructura JSON: "
				+ "{\"tipo\":\"RECETA\",\"nombre\":\"...\",\"porciones\":" + porciones + ",\"tiempo_preparacion\":\"...\",\"dificultad\":\"...\","
				+ "\"ingredientes\":[{\"nombre\":\"...\",\"cantidad\":\"...\"}],"
				+ "\"pasos\":[\"paso 1 en una sola frase\",\"paso 2 en una sola frase\"]}. "
				+ "REGLAS OBLIGATORIAS: "
				+ "1. El campo tipo DEBE ser exactamente RECETA. "
				+ "2. El campo porciones DEBE ser exactamente " + porciones + ". "
				+ "3. Las cantidades de TODOS los ingredientes deben estar calculadas para " + porciones + " personas. "
				+ "4. No incluyas materiales ni tiempo_estimado. "
				+ "5. No generes manualidades bajo ninguna circunstancia. "
				+ "6. Los pasos deben ser strings simples, NO objetos. Maximo 5 pasos. "
				+ "Responde SOLO con JSON valido, sin markdown ni texto adicional. "
				+ "Mensaje del usuario: " + prompt;
	}

	/**
	 * Construye el prompt para Google Gemini segun la categoria esperada.
	 *
	 * @param prompt descripcion escrita por el usuario
	 * @param tipoReceta tipo de contenido esperado
	 * @param porciones numero de porciones para recetas
	 * @return prompt final para Gemini
	 */
	private String construirPromptGemini(String prompt, String tipoReceta, Integer porciones) {
		String categoria = categoriaEsperada(tipoReceta);
		String error = mensajeErrorCategoria(tipoReceta);

		if ("MANUALIDAD".equals(categoria)) {
			return "Eres un experto exclusivamente en manualidades. "
					+ "Este campo SOLO recibe MANUALIDADES. "
					+ "Clasifica primero la solicitud del usuario. "
					+ "Acepta solo proyectos manuales hechos con materiales: arte, decoracion, reciclaje, arreglos, objetos o actividades creativas. "
					+ "Rechaza recetas de cocina, comidas, bebidas, postres, preparaciones comestibles o instrucciones para cocinar. "
					+ "Si no es una manualidad valida, responde SOLO con: "
					+ "{\"error\":\"" + error + "\"}. "
					+ "Si es manualidad valida, responde con EXACTAMENTE esta estructura JSON: "
					+ "{\"tipo\":\"MANUALIDAD\",\"nombre\":\"nombre\",\"dificultad\":\"Facil/Media/Dificil\",\"tiempo_estimado\":\"X minutos\","
					+ "\"materiales\":[{\"nombre\":\"material\",\"cantidad\":\"cantidad\"}],"
					+ "\"pasos\":[\"paso 1\",\"paso 2\"]}. "
					+ "REGLAS OBLIGATORIAS: "
					+ "1. El campo tipo DEBE ser exactamente MANUALIDAD. "
					+ "2. No uses campos de receta como ingredientes, porciones o tiempo_preparacion. "
					+ "3. No conviertas recetas en manualidades. "
					+ "4. Maximo 5 pasos. "
					+ "Responde UNICAMENTE con el objeto JSON, sin texto adicional, sin markdown, sin bloques de codigo. En espanol.\n\n"
					+ "Solicitud del usuario: " + prompt;
		}

		return "Eres un chef experto exclusivamente en recetas de cocina. "
				+ "Este campo SOLO recibe RECETAS DE COCINA. "
				+ "Clasifica primero la solicitud del usuario. "
				+ "Acepta solo preparaciones comestibles o bebidas con ingredientes y pasos de cocina. "
				+ "Rechaza manualidades, decoraciones, objetos, arreglos, arte, reciclaje o proyectos con materiales no comestibles. "
				+ "Si no es una receta de cocina valida, responde SOLO con: "
				+ "{\"error\":\"" + error + "\"}. "
				+ "Si es receta valida, responde con EXACTAMENTE esta estructura JSON: "
				+ "{\"tipo\":\"RECETA\",\"nombre\":\"nombre del plato\",\"porciones\":" + porciones + ",\"tiempo_preparacion\":\"X minutos\",\"dificultad\":\"Facil/Media/Dificil\","
				+ "\"ingredientes\":[{\"nombre\":\"ingrediente\",\"cantidad\":\"cantidad para " + porciones + " personas\"}],"
				+ "\"pasos\":[\"paso 1\",\"paso 2\"]}. "
				+ "REGLAS OBLIGATORIAS: "
				+ "1. El campo tipo DEBE ser exactamente RECETA. "
				+ "2. El campo porciones DEBE ser exactamente el numero " + porciones + ". "
				+ "3. Todas las cantidades de ingredientes DEBEN estar calculadas para " + porciones + " personas. "
				+ "4. No uses campos de manualidad como materiales o tiempo_estimado. "
				+ "5. No conviertas manualidades en recetas. "
				+ "6. Maximo 5 pasos. "
				+ "Responde UNICAMENTE con el objeto JSON, sin texto adicional, sin markdown, sin bloques de codigo. En espanol.\n\n"
				+ "Solicitud del usuario: " + prompt;
	}
}
