package co.edu.unbosque.artcook.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import co.edu.unbosque.artcook.services.VideoService;

@RunWith(MockitoJUnitRunner.class)
public class VideoServiceTest {

	@InjectMocks
	private VideoService videoService;

	public static int contar = 0;
	private AutoCloseable closeable;

	@BeforeClass
	public static void antesTodo() {
		System.out.println("Inicio pruebas unitarias VideoService");
	}

	@Before
	public void antes() throws Exception {
		contar++;
		System.out.println("Inicio prueba: " + contar);
		closeable = MockitoAnnotations.openMocks(this);

		String rutaTemporal = Paths.get(System.getProperty("java.io.tmpdir"), "artcook-test-videos").toString();

		setCampo(videoService, "videosPath", rutaTemporal);
		setCampo(videoService, "falAiApiKey", "fake-key");
		setCampo(videoService, "deepgramApiKey", "fake-key");
		setCampo(videoService, "ffmpegPath", "ffmpeg");
	}

	private void setCampo(Object objeto, String nombreCampo, Object valor) throws Exception {
		Field campo = objeto.getClass().getDeclaredField(nombreCampo);
		campo.setAccessible(true);
		campo.set(objeto, valor);
	}

	@Test
	public void testGenerarVideo_jsonSinPasos_lanzaException() {
		String json = "{\"tipo\":\"RECETA\",\"nombre\":\"Arroz con pollo\"}";

		try {
			videoService.generarVideo(json, "Arroz con pollo");
			fail("Debia lanzar exception porque no hay pasos.");
		} catch (Exception e) {
			assertEquals("No se encontraron pasos en el JSON de la receta.", e.getMessage());
		}
	}

	@Test
	public void testGenerarVideo_jsonConMarkdownSinPasos_lanzaException() {
		String json = "```json\n{\"tipo\":\"RECETA\",\"nombre\":\"Arroz con pollo\"}\n```";

		try {
			videoService.generarVideo(json, "Arroz con pollo");
			fail("Debia lanzar exception porque no hay pasos.");
		} catch (Exception e) {
			assertEquals("No se encontraron pasos en el JSON de la receta.", e.getMessage());
		}
	}

	@Test
	public void testGenerarVideo_jsonInvalido_lanzaException() {
		String json = "{json malo";

		try {
			videoService.generarVideo(json, "Receta mala");
			fail("Debia lanzar exception por JSON invalido.");
		} catch (Exception e) {
			assertTrue(e.getMessage() != null);
		}
	}

	@After
	public void despues() throws Exception {
		closeable.close();
		System.out.println("Fin prueba: " + contar);
	}

	@AfterClass
	public static void despuesTodo() {
		System.out.println("Fin pruebas unitarias VideoService");
	}
}