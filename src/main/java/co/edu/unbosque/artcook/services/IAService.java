package co.edu.unbosque.artcook.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import co.edu.unbosque.artcook.exception.CampoVacioException;
import co.edu.unbosque.artcook.exception.LanzadorExcepciones;
import co.edu.unbosque.artcook.exception.PromptVacioException;

@Service
public class IAService {
	
	@Value("${app.openai.api-key}")
	private String openaiApiKey;
	
	@Value("${app.gemini.api-key}")
	private String geminiApiKey;
	
	@Value("${app.claude.api-key}")
	private String claudeApiKey;
	
	private RestTemplate restTemplate = new RestTemplate();
	
	public String generarRecetaConGPT(String prompt) throws PromptVacioException {
		try {
			LanzadorExcepciones.validarPrompt(prompt);
			
			String respuesta = "Receta generada por GPT desde prompt: " + prompt;
			return respuesta;
		} catch (PromptVacioException e) {
			throw e;
		}
	}
	
	public String generarRecetaConGemini(String prompt) throws PromptVacioException {
		try {
			LanzadorExcepciones.validarPrompt(prompt);
			
			String respuesta = "Receta generada por Gemini desde prompt: " + prompt;
			return respuesta;
		} catch (PromptVacioException e) {
			throw e;
		}
	}
	
	public String generarRecetaConClaude(String prompt) throws PromptVacioException {
		try {
			LanzadorExcepciones.validarPrompt(prompt);
			
			String respuesta = "Receta generada por Claude desde prompt: " + prompt;
			return respuesta;
		} catch (PromptVacioException e) {
			throw e;
		}
	}
	
	public String generarNarracionConIA(String textoReceta) throws CampoVacioException {
		try {
			LanzadorExcepciones.validarCampoVacio(textoReceta, "textoReceta");
			
			String respuesta = "Narración generada desde receta";
			return respuesta;
		} catch (CampoVacioException e) {
			throw e;
		}
	}
	
	public boolean verificarConexion() {
		try {
			return !openaiApiKey.equals("pending") && 
					!geminiApiKey.equals("pending") && 
					!claudeApiKey.equals("pending");
		} catch (Exception e) {
			return false;
		}
	}
}