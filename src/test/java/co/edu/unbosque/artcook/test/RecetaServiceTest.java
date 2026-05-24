package co.edu.unbosque.artcook.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import co.edu.unbosque.artcook.dto.RecetaDTO;
import co.edu.unbosque.artcook.dto.TipoRecetaDTO;
import co.edu.unbosque.artcook.entity.Receta;
import co.edu.unbosque.artcook.entity.TipoReceta;
import co.edu.unbosque.artcook.exception.RegistroNoEncontradoException;
import co.edu.unbosque.artcook.exception.TipoRecetaException;
import co.edu.unbosque.artcook.repository.RecetaRepository;
import co.edu.unbosque.artcook.services.RecetaService;

@RunWith(MockitoJUnitRunner.class)
public class RecetaServiceTest {

	@Mock
	private RecetaRepository recetaRepo;

	@Mock
	private ModelMapper mapper;

	@InjectMocks
	private RecetaService recetaService;

	public static int contar = 0;
	private AutoCloseable closeable;

	@BeforeClass
	public static void antesTodo() {
		System.out.println("Inicio pruebas unitarias RecetaService");
	}

	@Before
	public void antes() {
		contar++;
		System.out.println("Inicio prueba: " + contar);
		closeable = MockitoAnnotations.openMocks(this);
	}

	private RecetaDTO crearDTO() {
		RecetaDTO dto = new RecetaDTO();
		dto.setTitulo("Arroz con pollo");
		dto.setPromptOriginal("prepara arroz con pollo casero");
		dto.setTipoReceta(TipoRecetaDTO.COCINA);
		dto.setUsuarioId(1L);
		dto.setPorciones(2);
		dto.setJsonRecetaGPT("{\"nombre\":\"Arroz con pollo\"}");
		dto.setJsonRecetaGemini("{\"nombre\":\"Arroz con pollo Gemini\"}");
		dto.setJsonRecetaClaude("{\"nombre\":\"Arroz con pollo Claude\"}");
		dto.setJsonRecetaSeleccionada("{\"nombre\":\"Arroz con pollo\"}");
		dto.setIaSeleccionada("gpt");
		return dto;
	}

	private Receta crearEntidad() {
		Receta receta = new Receta("Arroz con pollo", "prepara arroz con pollo casero", TipoReceta.COCINA, 1L);
		receta.setPorciones(2);
		receta.setJsonRecetaGPT("{\"tipo\":\"RECETA\",\"nombre\":\"Arroz con pollo\",\"porciones\":2,\"ingredientes\":[{\"nombre\":\"arroz\",\"cantidad\":\"1 taza\"}],\"pasos\":[\"Cocinar el arroz\"]}");
		receta.setJsonRecetaGemini("{\"nombre\":\"Gemini\"}");
		receta.setJsonRecetaClaude("{\"nombre\":\"Claude\"}");
		receta.setJsonRecetaSeleccionada(receta.getJsonRecetaGPT());
		receta.setIaSeleccionada("gpt");
		receta.setFechaCreacion(LocalDateTime.now());
		receta.setActiva(true);
		return receta;
	}

	@Test
	public void testCreate_exitoso() {
		RecetaDTO dto = crearDTO();

		when(recetaRepo.save(any(Receta.class))).thenAnswer(i -> i.getArgument(0));

		int result = recetaService.create(dto);

		assertEquals(0, result);
		verify(recetaRepo).save(any(Receta.class));
	}

	@Test
	public void testCreate_promptVacio_retorna1() {
		RecetaDTO dto = crearDTO();
		dto.setPromptOriginal("");

		int result = recetaService.create(dto);

		assertEquals(1, result);
	}

	@Test
	public void testGetAll_retornaLista() {
		Receta r1 = crearEntidad();
		Receta r2 = crearEntidad();

		RecetaDTO dto1 = new RecetaDTO();
		RecetaDTO dto2 = new RecetaDTO();

		when(recetaRepo.findAll()).thenReturn(Arrays.asList(r1, r2));
		when(mapper.map(r1, RecetaDTO.class)).thenReturn(dto1);
		when(mapper.map(r2, RecetaDTO.class)).thenReturn(dto2);

		List<RecetaDTO> result = recetaService.getAll();

		assertTrue(result != null && result.size() == 2);
	}

	@Test
	public void testDeleteByID_exitoso() {
		when(recetaRepo.existsById(1L)).thenReturn(true);
		doNothing().when(recetaRepo).deleteById(1L);

		int result = recetaService.deleteByID(1L);

		assertEquals(0, result);
	}

	@Test
	public void testDeleteByID_noExiste_retorna1() {
		when(recetaRepo.existsById(99L)).thenReturn(false);

		int result = recetaService.deleteByID(99L);

		assertEquals(1, result);
	}

	@Test
	public void testUpdateByID_exitoso() {
		Receta receta = crearEntidad();
		RecetaDTO dto = new RecetaDTO();
		dto.setTitulo("Nuevo titulo");
		dto.setIaSeleccionada("gemini");
		dto.setJsonRecetaSeleccionada("{\"nombre\":\"Nueva receta\"}");

		when(recetaRepo.findById(1L)).thenReturn(Optional.of(receta));
		when(recetaRepo.save(receta)).thenReturn(receta);

		int result = recetaService.updateByID(1L, dto);

		assertEquals(0, result);
		assertEquals("Nuevo titulo", receta.getTitulo());
	}

	@Test
	public void testUpdateByID_noExiste_retorna1() {
		when(recetaRepo.findById(99L)).thenReturn(Optional.empty());

		int result = recetaService.updateByID(99L, new RecetaDTO());

		assertEquals(1, result);
	}

	@Test
	public void testCount_retornaCantidad() {
		when(recetaRepo.count()).thenReturn(4L);

		long result = recetaService.count();

		assertEquals(4L, result);
	}

	@Test
	public void testExist_idExistente_retornaTrue() {
		when(recetaRepo.existsById(1L)).thenReturn(true);

		boolean result = recetaService.exist(1L);

		assertEquals(true, result);
	}

	@Test
	public void testObtenerPorId_exitoso() throws RegistroNoEncontradoException {
		Receta receta = crearEntidad();
		RecetaDTO dto = crearDTO();

		when(recetaRepo.findById(1L)).thenReturn(Optional.of(receta));
		when(mapper.map(receta, RecetaDTO.class)).thenReturn(dto);

		RecetaDTO result = recetaService.obtenerPorId(1L);

		assertEquals(dto, result);
	}

	@Test(expected = RegistroNoEncontradoException.class)
	public void testObtenerPorId_noExiste_lanzaException() throws RegistroNoEncontradoException {
		when(recetaRepo.findById(99L)).thenReturn(Optional.empty());

		recetaService.obtenerPorId(99L);
	}

	@Test
	public void testObtenerPorUsuario_retornaLista() throws RegistroNoEncontradoException {
		Receta receta = crearEntidad();
		RecetaDTO dto = crearDTO();

		when(recetaRepo.findByUsuarioId(1L)).thenReturn(Arrays.asList(receta));
		when(mapper.map(receta, RecetaDTO.class)).thenReturn(dto);

		List<RecetaDTO> result = recetaService.obtenerPorUsuario(1L);

		assertEquals(1, result.size());
	}

	@Test
	public void testObtenerPorTipo_retornaLista() throws TipoRecetaException {
		Receta receta = crearEntidad();
		RecetaDTO dto = crearDTO();

		when(recetaRepo.findByTipoReceta(TipoReceta.COCINA)).thenReturn(Arrays.asList(receta));
		when(mapper.map(receta, RecetaDTO.class)).thenReturn(dto);

		List<RecetaDTO> result = recetaService.obtenerPorTipo("COCINA");

		assertEquals(1, result.size());
	}

	@Test
	public void testSeleccionarReceta_gptExitoso() {
		Receta receta = crearEntidad();

		when(recetaRepo.findById(1L)).thenReturn(Optional.of(receta));
		when(recetaRepo.save(receta)).thenReturn(receta);

		int result = recetaService.seleccionarReceta(1L, "gpt");

		assertEquals(0, result);
		assertEquals("gpt", receta.getIaSeleccionada());
	}

	@Test
	public void testSeleccionarReceta_noExiste_retorna1() {
		when(recetaRepo.findById(99L)).thenReturn(Optional.empty());

		int result = recetaService.seleccionarReceta(99L, "gpt");

		assertEquals(1, result);
	}

	@Test
	public void testSeleccionarReceta_sinContenido_retorna2() {
		Receta receta = crearEntidad();
		receta.setJsonRecetaGPT(null);

		when(recetaRepo.findById(1L)).thenReturn(Optional.of(receta));

		int result = recetaService.seleccionarReceta(1L, "gpt");

		assertEquals(2, result);
	}

	@Test
	public void testSeleccionarReceta_iaInvalida_retorna2() {
		Receta receta = crearEntidad();

		when(recetaRepo.findById(1L)).thenReturn(Optional.of(receta));

		int result = recetaService.seleccionarReceta(1L, "otra");

		assertEquals(2, result);
	}

	@Test
	public void testDesactivarReceta_exitoso() throws RegistroNoEncontradoException {
		Receta receta = crearEntidad();

		when(recetaRepo.findById(1L)).thenReturn(Optional.of(receta));
		when(recetaRepo.save(receta)).thenReturn(receta);

		int result = recetaService.desactivarReceta(1L);

		assertEquals(0, result);
		assertEquals(false, receta.isActiva());
	}

	@Test(expected = RegistroNoEncontradoException.class)
	public void testDesactivarReceta_noExiste_lanzaException() throws RegistroNoEncontradoException {
		when(recetaRepo.findById(99L)).thenReturn(Optional.empty());

		recetaService.desactivarReceta(99L);
	}

	@Test
	public void testGenerarPdfDesdeReceta_retornaBytes() {
		Receta receta = crearEntidad();

		byte[] result = recetaService.generarPdfDesdeReceta(receta);

		assertTrue(result != null && result.length > 0);
	}

	@Test
	public void testGenerarPdfReceta_exitoso() throws RegistroNoEncontradoException {
		Receta receta = crearEntidad();

		when(recetaRepo.findById(1L)).thenReturn(Optional.of(receta));

		byte[] result = recetaService.generarPdfReceta(1L);

		assertTrue(result != null && result.length > 0);
	}

	@After
	public void despues() throws Exception {
		closeable.close();
		System.out.println("Fin prueba: " + contar);
	}

	@AfterClass
	public static void despuesTodo() {
		System.out.println("Fin pruebas unitarias RecetaService");
	}
}