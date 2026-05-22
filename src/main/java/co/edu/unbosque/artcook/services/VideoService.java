package co.edu.unbosque.artcook.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class VideoService {

	@Value("${app.falai.api-key}")
	private String falAiApiKey;

	@Value("${deepgram.api.key}")
	private String deepgramApiKey;

	@Value("${app.videos.path}")
	private String videosPath;

	@Value("${app.ffmpeg.path}")
	private String ffmpegPath;

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public String generarVideo(String jsonReceta, String nombreReceta) throws Exception {

		Files.createDirectories(Paths.get(videosPath));

		String jsonLimpio = jsonReceta.replaceAll("(?s)^```json\\s*", "").replaceAll("(?s)```\\s*$", "").trim();

		JsonNode raiz = objectMapper.readTree(jsonLimpio);

		String tipo = "";
		if (raiz.has("tipo")) {
			tipo = raiz.get("tipo").asText().toLowerCase();
		}

		JsonNode pasosNode = null;
		if (raiz.has("pasos") && raiz.get("pasos").isArray()) {
			pasosNode = raiz.get("pasos");
		} else if (raiz.has("pasos_simplificados") && raiz.get("pasos_simplificados").isArray()) {
			pasosNode = raiz.get("pasos_simplificados");
		}

		if (pasosNode == null) {
			throw new Exception("No se encontraron pasos en el JSON de la receta.");
		}

		String nombrePlato = nombreReceta;
		if (raiz.has("nombre")) {
			nombrePlato = raiz.get("nombre").asText();
		}

		List<String> textosPasos = new ArrayList<>();
		List<String> promptsImagenes = new ArrayList<>();

		for (JsonNode paso : pasosNode) {
			if (paso.isTextual()) {
				String textoPaso = paso.asText();
				textosPasos.add(textoPaso);
				promptsImagenes.add(textoPaso);
			} else if (paso.isObject()) {
				String descripcion = paso.has("descripcion") ? paso.get("descripcion").asText() : "";
				String tecnica = paso.has("tecnica") ? paso.get("tecnica").asText() : "";
				textosPasos.add(descripcion);
				promptsImagenes.add(descripcion + (tecnica.isEmpty() ? "" : ". " + tecnica));
			}
		}

		String carpetaVideo = videosPath + "/" + UUID.randomUUID();
		Files.createDirectories(Paths.get(carpetaVideo));

		List<String> rutasImagenes = new ArrayList<>();
		for (int i = 0; i < promptsImagenes.size(); i++) {
			String rutaImagen = generarImagenConFalAi(promptsImagenes.get(i), nombrePlato, carpetaVideo, i, tipo);
			rutasImagenes.add(rutaImagen);
		}

		String textoCompleto = String.join(". ", textosPasos);
		String rutaAudio = generarAudioConDeepgram(textoCompleto, carpetaVideo);

		return combinarConFFmpeg(rutasImagenes, rutaAudio, carpetaVideo, nombreReceta);
	}

	private String generarImagenConFalAi(String textoPaso, String nombrePlato, String carpeta, int indice, String tipo)
			throws Exception {

		String url = "https://fal.run/fal-ai/flux/dev";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Key " + falAiApiKey);

		String prompt;
		if (tipo.contains("manualidad") || tipo.contains("craft") || tipo.contains("art")) {
			prompt = "Professional craft photography of a DIY project called " + nombrePlato + ". "
					+ "Step shown: " + textoPaso + ". "
					+ "Close-up shot, natural lighting, hands crafting, workshop table background, "
					+ "craft materials visible, high resolution, editorial style, warm tones.";
		} else {
			prompt = "Professional food photography of " + nombrePlato + ". "
					+ "Step shown: " + textoPaso + ". "
					+ "Close-up shot, natural lighting, appetizing presentation, "
					+ "rustic kitchen background, soft bokeh, warm tones, high resolution, editorial food photo.";
		}

		Map<String, Object> body = new HashMap<>();
		body.put("prompt", prompt);
		body.put("image_size", "landscape_16_9");
		body.put("num_images", 1);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

		JsonNode respuestaJson = objectMapper.readTree(response.getBody());
		String urlImagen = respuestaJson.get("images").get(0).get("url").asText();

		String rutaImagen = carpeta + "/paso_" + indice + ".jpg";
		byte[] imagenBytes = restTemplate.getForObject(urlImagen, byte[].class);
		Files.write(Paths.get(rutaImagen), imagenBytes);

		return rutaImagen;
	}

	private String generarAudioConDeepgram(String texto, String carpeta) throws Exception {

		try {
			String url = "https://api.deepgram.com/v1/speak?model=aura-2-celeste-es";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "Token " + deepgramApiKey);

			Map<String, String> body = new HashMap<>();
			body.put("text", texto);

			HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

			ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, request, byte[].class);

			String rutaAudio = carpeta + "/audio.mp3";
			Files.write(Paths.get(rutaAudio), response.getBody());

			return rutaAudio;

		} catch (Exception e) {
			throw new RuntimeException("Error al generar el audio con Deepgram: " + e.getMessage(), e);
		}
	}

	private String combinarConFFmpeg(List<String> rutasImagenes, String rutaAudio, String carpeta, String nombreReceta)
			throws Exception {

		String archivoLista = carpeta + "/imagenes.txt";
		StringBuilder lista = new StringBuilder();

		int segundosPorPaso = 5;
		for (String rutaImagen : rutasImagenes) {
			lista.append("file '").append(Paths.get(rutaImagen).toAbsolutePath().toString().replace("\\", "/"))
					.append("'\n");
			lista.append("duration ").append(segundosPorPaso).append("\n");
		}
		lista.append("file '")
				.append(Paths.get(rutasImagenes.get(rutasImagenes.size() - 1)).toAbsolutePath().toString()
						.replace("\\", "/"))
				.append("'\n");

		Files.writeString(Paths.get(archivoLista), lista.toString());

		String nombreArchivo = nombreReceta.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
		String rutaVideo = Paths.get(videosPath).toAbsolutePath() + "/" + nombreArchivo + "_" + UUID.randomUUID()
				+ ".mp4";

		List<String> comando = List.of(
				ffmpegPath, "-f", "concat", "-safe", "0",
				"-i", Paths.get(archivoLista).toAbsolutePath().toString().replace("\\", "/"),
				"-i", Paths.get(rutaAudio).toAbsolutePath().toString().replace("\\", "/"),
				"-c:v", "libx264", "-c:a", "aac", "-pix_fmt", "yuv420p",
				"-y", rutaVideo.replace("\\", "/")
		);

		ProcessBuilder processBuilder = new ProcessBuilder(comando);
		processBuilder.redirectErrorStream(true);
		Process proceso = processBuilder.start();

		String salidaFFmpeg = new String(proceso.getInputStream().readAllBytes());
		int codigoSalida = proceso.waitFor();

		if (codigoSalida != 0) {
			throw new RuntimeException("Error al procesar el video con FFmpeg: " + salidaFFmpeg);
		}

		return Paths.get(rutaVideo).getFileName().toString();
	}
}
}