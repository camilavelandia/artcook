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
 * Servicio que gestiona las llamadas a las tres IAs del sistema.
 * Conecta con OpenAI (GPT), Google (Gemini) y Anthropic (Claude).
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
     * @param prompt prompt de la receta o manualidad
     * @return contenido generado por GPT
     * @throws PromptVacioException si el prompt es nulo, vacío o muy corto
     */
    public String generarRecetaConGPT(String prompt) throws PromptVacioException {
        LanzadorExcepciones.validarPrompt(prompt);
        if (openAiApiKey.equals("pending")) {
            return "OpenAI no esta configurado aun.";
        }
        try {
            String url = "https://api.openai.com/v1/chat/completions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                    Map.of("role", "system", "content",
                        "Eres un experto en cocina y manualidades. Genera recetas detalladas con ingredientes, pasos y consejos."),
                    Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 1500
            );

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
     * @param prompt prompt de la receta o manualidad
     * @return contenido generado por Gemini
     * @throws PromptVacioException si el prompt es nulo, vacío o muy corto
     */
    public String generarRecetaConGemini(String prompt) throws PromptVacioException {
        LanzadorExcepciones.validarPrompt(prompt);
        if (geminiApiKey.equals("pending")) {
            return "Gemini no esta configurado aun.";
        }
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of("maxOutputTokens", 1500, "temperature", 0.7)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            Map response = restTemplate.postForObject(url, request, Map.class);

            List candidates = (List) response.get("candidates");
            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            Map part = (Map) parts.get(0);
            return (String) part.get("text");

        } catch (Exception e) {
            return "Error al conectar con Gemini: " + e.getMessage();
        }
    }

    /**
     * Genera contenido usando Anthropic Claude con el prompt dado.
     *
     * @param prompt prompt de la receta o manualidad
     * @return contenido generado por Claude
     * @throws PromptVacioException si el prompt es nulo, vacío o muy corto
     */
    public String generarRecetaConClaude(String prompt) throws PromptVacioException {
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

            Map<String, Object> body = Map.of(
                "model", "claude-haiku-4-5-20251001",
                "max_tokens", 1500,
                "messages", List.of(Map.of("role", "user", "content", prompt))
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            Map response = restTemplate.postForObject(url, request, Map.class);

            List content = (List) response.get("content");
            Map block = (Map) content.get(0);
            return (String) block.get("text");

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
            return generarRecetaConGPT("Genera una narración clara y amigable para esta receta: " + textoReceta);
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
        return !openAiApiKey.equals("pending")
                && !geminiApiKey.equals("pending")
                && !claudeApiKey.equals("pending");
    }

    /**
     * Construye el prompt completo con instrucciones según el tipo de receta.
     *
     * @param prompt     prompt base del usuario
     * @param tipoReceta tipo de receta (COCINA o MANUALIDAD)
     * @return prompt enriquecido con instrucciones específicas
     */
    public String construirInstruccion(String prompt, String tipoReceta) {
        if (tipoReceta.equalsIgnoreCase("COCINA")) {
            return "Genera una receta detallada de cocina para: " + prompt + "\n\n"
                + "Incluye:\n"
                + "- Titulo de la receta\n"
                + "- Tiempo de preparacion\n"
                + "- Porciones\n"
                + "- Lista de ingredientes con cantidades\n"
                + "- Pasos de preparacion numerados\n"
                + "- Consejos adicionales\n"
                + "- Informacion nutricional aproximada";
        } else {
            return "Genera instrucciones detalladas para la manualidad: " + prompt + "\n\n"
                + "Incluye:\n"
                + "- Titulo de la manualidad\n"
                + "- Nivel de dificultad\n"
                + "- Tiempo estimado\n"
                + "- Materiales necesarios con cantidades\n"
                + "- Pasos detallados numerados\n"
                + "- Consejos y variaciones posibles";
        }
    }
}
