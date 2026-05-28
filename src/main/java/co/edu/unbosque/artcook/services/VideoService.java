package co.edu.unbosque.artcook.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Servicio encargado de generar videos a partir del contenido de una receta.
 * Coordina la generación de imágenes con Fal.ai, el audio con Deepgram y la
 * combinación de ambos en un video final usando JavaCV (sin necesidad de tener
 * FFmpeg instalado en el servidor).
 */
@Service
public class VideoService {

	/** Clave de API para el servicio de generación de imágenes Fal.ai. */
	@Value("${app.falai.api-key}")
	private String falAiApiKey;

	/** Clave de API para el servicio de generación de audio Deepgram. */
	@Value("${deepgram.api.key}")
	private String deepgramApiKey;

	/** Ruta del sistema de archivos donde se almacenan los videos generados. */
	@Value("${app.videos.path}")
	private String videosPath;

	/** Cliente HTTP para realizar peticiones a las APIs externas. */
	private final RestTemplate restTemplate = new RestTemplate();

	/** Mapper para serializar y deserializar objetos JSON. */
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Genera un video completo a partir del JSON de una receta. Extrae los pasos,
	 * genera una imagen por paso con Fal.ai, genera el audio con Deepgram y combina
	 * todo con JavaCV.
	 *
	 * @param jsonReceta   contenido de la receta en formato JSON
	 * @param nombreReceta nombre de la receta para identificar el video generado
	 * @return nombre del archivo de video generado
	 * @throws Exception si ocurre un error durante cualquier etapa de la generación
	 */
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

		return combinarConJavaCV(rutasImagenes, rutaAudio, carpetaVideo, nombreReceta);
	}

	/**
	 * Genera una imagen para un paso de la receta usando la API de Fal.ai. Adapta
	 * el prompt según si la receta es de cocina o manualidad.
	 *
	 * @param textoPaso   descripción del paso a ilustrar
	 * @param nombrePlato nombre del plato o manualidad
	 * @param carpeta     ruta de la carpeta donde se guarda la imagen
	 * @param indice      número del paso para nombrar el archivo
	 * @param tipo        tipo de receta para adaptar el estilo de la imagen
	 * @return ruta local donde se guardó la imagen generada
	 * @throws Exception si ocurre un error al llamar la API o guardar la imagen
	 */
	private String generarImagenConFalAi(String textoPaso, String nombrePlato, String carpeta, int indice, String tipo)
			throws Exception {

		String url = "https://fal.run/fal-ai/flux/dev";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Key " + falAiApiKey);

		String prompt;
		if (tipo.contains("manualidad") || tipo.contains("craft") || tipo.contains("art")) {
		    prompt = "Step-by-step craft tutorial photo. "
		            + "The exact action happening RIGHT NOW: " + textoPaso + ". "
		            + "Show ONLY this specific step in progress, hands actively doing the action, "
		            + "close-up of the materials being worked on at this exact moment. "
		            + "Project name: " + nombrePlato + ". "
		            + "Natural lighting, workshop table, high resolution. "
		            + "Do NOT show the finished project.";
		} else {
		    prompt = "Step-by-step cooking tutorial photo. "
		            + "The exact action happening RIGHT NOW: " + textoPaso + ". "
		            + "Show ONLY this specific cooking step in progress, "
		            + "close-up of the ingredients and utensils being used at this exact moment. "
		            + "Recipe name: " + nombrePlato + ". "
		            + "Natural kitchen lighting, high resolution. "
		            + "Do NOT show the finished dish.";
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

	/**
	 * Genera un archivo de audio a partir del texto de los pasos usando la API de
	 * Deepgram.
	 *
	 * @param texto   texto completo de los pasos de la receta
	 * @param carpeta ruta de la carpeta donde se guarda el audio generado
	 * @return ruta local donde se guardó el archivo de audio
	 * @throws Exception si ocurre un error al llamar la API o guardar el audio
	 */
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

	/**
	 * Combina las imágenes y el audio generados en un video usando JavaCV. No
	 * requiere FFmpeg instalado en el servidor: los binarios vienen empaquetados
	 * dentro de la dependencia javacv-platform. Cada imagen se muestra durante
	 * cinco segundos en el video final.
	 *
	 * @param rutasImagenes lista de rutas locales de las imágenes generadas
	 * @param rutaAudio     ruta local del archivo de audio generado
	 * @param carpeta       ruta de la carpeta de trabajo temporal
	 * @param nombreReceta  nombre de la receta para nombrar el archivo de video
	 * @return nombre del archivo de video generado
	 * @throws Exception si ocurre un error durante el procesamiento del video
	 */
	private String combinarConJavaCV(List<String> rutasImagenes, String rutaAudio, String carpeta, String nombreReceta)
			throws Exception {

		int segundosPorPaso = 5;
		int fps = 1;
		int ancho = 1280;
		int alto = 720;

		String nombreArchivo = nombreReceta.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();

		String rutaVideoSinAudio = carpeta + "/" + nombreArchivo + "_sin_audio.mp4";

		String rutaVideoFinal = Paths.get(videosPath).toAbsolutePath() + "/" + nombreArchivo + "_" + UUID.randomUUID()
				+ ".mp4";

		FFmpegFrameRecorder recorderVideo = new FFmpegFrameRecorder(rutaVideoSinAudio, ancho, alto);
		recorderVideo.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		recorderVideo.setFormat("mp4");
		recorderVideo.setFrameRate(fps);
		recorderVideo.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
		recorderVideo.start();

		Java2DFrameConverter converter = new Java2DFrameConverter();

		for (String rutaImagen : rutasImagenes) {
			BufferedImage imgOriginal = ImageIO.read(new File(rutaImagen));

			BufferedImage imgRedimensionada = new BufferedImage(ancho, alto, BufferedImage.TYPE_3BYTE_BGR);
			imgRedimensionada.getGraphics().drawImage(imgOriginal.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH), 0,
					0, null);

			Frame frame = converter.convert(imgRedimensionada);

			for (int f = 0; f < segundosPorPaso * fps; f++) {
				recorderVideo.record(frame);
			}
		}

		recorderVideo.stop();
		recorderVideo.release();

		FFmpegFrameGrabber grabberVideo = new FFmpegFrameGrabber(rutaVideoSinAudio);
		FFmpegFrameGrabber grabberAudio = new FFmpegFrameGrabber(rutaAudio);

		grabberVideo.start();
		grabberAudio.start();

		FFmpegFrameRecorder recorderFinal = new FFmpegFrameRecorder(rutaVideoFinal, ancho, alto);
		recorderFinal.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		recorderFinal.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
		recorderFinal.setFormat("mp4");
		recorderFinal.setFrameRate(fps);
		recorderFinal.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
		recorderFinal.setSampleRate(grabberAudio.getSampleRate());
		recorderFinal.setAudioChannels(grabberAudio.getAudioChannels());
		recorderFinal.start();

		Frame frameVideo;
		while ((frameVideo = grabberVideo.grabFrame()) != null) {
			recorderFinal.record(frameVideo);
		}

		Frame frameAudio;
		while ((frameAudio = grabberAudio.grabFrame()) != null) {
			recorderFinal.record(frameAudio);
		}

		grabberVideo.stop();
		grabberAudio.stop();
		recorderFinal.stop();
		recorderFinal.release();

		Files.deleteIfExists(Paths.get(rutaVideoSinAudio));

		return Paths.get(rutaVideoFinal).getFileName().toString();
	}
}