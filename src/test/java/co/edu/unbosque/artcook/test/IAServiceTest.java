package co.edu.unbosque.artcook.test;

import static org.junit.Assert.assertEquals;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import co.edu.unbosque.artcook.exception.CampoVacioException;
import co.edu.unbosque.artcook.exception.PromptVacioException;
import co.edu.unbosque.artcook.services.IAService;

@RunWith(MockitoJUnitRunner.class)
public class IAServiceTest {

	@InjectMocks
	private IAService iaService;

	public static int contar = 0;
	private AutoCloseable closeable;

	@BeforeClass
	public static void antesTodo() {
		System.out.println("Inicio pruebas unitarias IAService");
	}

	@Before
	public void antes() throws Exception {
		contar++;
		System.out.println("Inicio prueba: " + contar);
		closeable = MockitoAnnotations.openMocks(this);
		setCampo(iaService, "openAiApiKey", "pending");
		setCampo(iaService, "geminiApiKey", "pending");
		setCampo(iaService, "claudeApiKey", "pending");
	}

	private void setCampo(Object objeto, String nombreCampo, Object valor) throws Exception {
		Field campo = objeto.getClass().getDeclaredField(nombreCampo);
		campo.setAccessible(true);
		campo.set(objeto, valor);
	}

	@Test
	public void testGenerarRecetaConGPT_sinConfigurar() throws PromptVacioException {
		String result = iaService.generarRecetaConGPT("prepara arroz con pollo casero", "COCINA", 2);

		assertEquals("OpenAI no esta configurado aun.", result);
	}

	@Test
	public void testGenerarRecetaConGemini_sinConfigurar() throws PromptVacioException {
		String result = iaService.generarRecetaConGemini("prepara pasta con queso", "COCINA", 2);

		assertEquals("Gemini no esta configurado aun.", result);
	}

	@Test
	public void testGenerarRecetaConClaude_sinConfigurar() throws PromptVacioException {
		String result = iaService.generarRecetaConClaude("prepara una sopa de verduras", "COCINA", 2);

		assertEquals("Claude no esta configurado aun.", result);
	}

	@Test(expected = PromptVacioException.class)
	public void testGenerarRecetaConGPT_promptVacio_lanzaException() throws PromptVacioException {
		iaService.generarRecetaConGPT("", "COCINA", 1);
	}

	@Test(expected = PromptVacioException.class)
	public void testGenerarRecetaConGemini_promptVacio_lanzaException() throws PromptVacioException {
		iaService.generarRecetaConGemini("", "COCINA", 1);
	}

	@Test(expected = PromptVacioException.class)
	public void testGenerarRecetaConClaude_promptVacio_lanzaException() throws PromptVacioException {
		iaService.generarRecetaConClaude("", "COCINA", 1);
	}

	@Test
	public void testGenerarNarracionConIA_textoValido() throws CampoVacioException {
		String result = iaService.generarNarracionConIA("Mezclar los ingredientes y hornear por veinte minutos.");

		assertEquals("OpenAI no esta configurado aun.", result);
	}

	@Test(expected = CampoVacioException.class)
	public void testGenerarNarracionConIA_textoVacio_lanzaException() throws CampoVacioException {
		iaService.generarNarracionConIA("");
	}

	@Test
	public void testVerificarConexion_sinKeys_retornaFalse() {
		boolean result = iaService.verificarConexion();

		assertEquals(false, result);
	}

	@Test
	public void testVerificarConexion_conTodasLasKeys_retornaTrue() throws Exception {
		setCampo(iaService, "openAiApiKey", "key-openai");
		setCampo(iaService, "geminiApiKey", "key-gemini");
		setCampo(iaService, "claudeApiKey", "key-claude");

		boolean result = iaService.verificarConexion();

		assertEquals(true, result);
	}

	@Test
	public void testGetGeminiKey_retornaKey() throws Exception {
		setCampo(iaService, "geminiApiKey", "mi-key");

		String result = iaService.getGeminiKey();

		assertEquals("mi-key", result);
	}

	@After
	public void despues() throws Exception {
		closeable.close();
		System.out.println("Fin prueba: " + contar);
	}

	@AfterClass
	public static void despuesTodo() {
		System.out.println("Fin pruebas unitarias IAService");
	}
}