package co.edu.unbosque.artcook.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
import co.edu.unbosque.artcook.dto.AuditoriaDTO;
import co.edu.unbosque.artcook.entity.Auditoria;
import co.edu.unbosque.artcook.exception.RegistroNoEncontradoException;
import co.edu.unbosque.artcook.repository.AuditoriaRepository;
import co.edu.unbosque.artcook.services.AuditoriaService;

@RunWith(MockitoJUnitRunner.class)
public class AuditoriaServiceTest {

	@Mock
	private AuditoriaRepository auditoriaRepo;

	@Mock
	private ModelMapper mapper;

	@InjectMocks
	private AuditoriaService auditoriaService;

	public static int contar = 0;
	private AutoCloseable closeable;

	@BeforeClass
	public static void antesTodo() {
		System.out.println("Inicio pruebas unitarias AuditoriaService");
	}

	@Before
	public void antes() {
		contar++;
		System.out.println("Inicio prueba: " + contar);
		closeable = MockitoAnnotations.openMocks(this);
	}

	private AuditoriaDTO crearDTO() {
		AuditoriaDTO dto = new AuditoriaDTO();
		dto.setUsuarioId(1L);
		dto.setAccion("CREAR");
		dto.setEntidad("Usuario");
		dto.setIdEntidad(10L);
		dto.setDetalles("Registro creado");
		return dto;
	}

	@Test
	public void testCreate_exitoso() {
		AuditoriaDTO dto = crearDTO();

		when(auditoriaRepo.save(any(Auditoria.class))).thenAnswer(i -> i.getArgument(0));

		int result = auditoriaService.create(dto);

		assertEquals(0, result);
		verify(auditoriaRepo).save(any(Auditoria.class));
	}

	@Test
	public void testCreate_usuarioIdInvalido_retorna1() {
		AuditoriaDTO dto = crearDTO();
		dto.setUsuarioId(0L);

		int result = auditoriaService.create(dto);

		assertEquals(1, result);
	}

	@Test
	public void testGetAll_retornaLista() {
		Auditoria a1 = new Auditoria();
		Auditoria a2 = new Auditoria();

		AuditoriaDTO dto1 = new AuditoriaDTO();
		AuditoriaDTO dto2 = new AuditoriaDTO();

		when(auditoriaRepo.findAll()).thenReturn(Arrays.asList(a1, a2));
		when(mapper.map(a1, AuditoriaDTO.class)).thenReturn(dto1);
		when(mapper.map(a2, AuditoriaDTO.class)).thenReturn(dto2);

		List<AuditoriaDTO> result = auditoriaService.getAll();

		assertTrue(result != null && result.size() == 2);
	}

	@Test
	public void testDeleteByID_retornaMenos1() {
		int result = auditoriaService.deleteByID(1L);

		assertEquals(-1, result);
	}

	@Test
	public void testUpdateByID_retornaMenos1() {
		int result = auditoriaService.updateByID(1L, crearDTO());

		assertEquals(-1, result);
	}

	@Test
	public void testCount_retornaCantidad() {
		when(auditoriaRepo.count()).thenReturn(5L);

		long result = auditoriaService.count();

		assertEquals(5L, result);
	}

	@Test
	public void testExist_idExistente_retornaTrue() {
		when(auditoriaRepo.existsById(1L)).thenReturn(true);

		boolean result = auditoriaService.exist(1L);

		assertEquals(true, result);
	}

	@Test
	public void testExist_idInexistente_retornaFalse() {
		when(auditoriaRepo.existsById(99L)).thenReturn(false);

		boolean result = auditoriaService.exist(99L);

		assertEquals(false, result);
	}

	@Test
	public void testRegistrarAccion_exitoso() {
		auditoriaService.registrarAccion(null, "LOGIN", "Usuario", 1L, "Inicio de sesion");

		verify(auditoriaRepo).save(any(Auditoria.class));
	}

	@Test
	public void testObtenerPorUsuario_retornaLista() throws RegistroNoEncontradoException {
		Auditoria auditoria = new Auditoria();
		AuditoriaDTO dto = new AuditoriaDTO();

		when(auditoriaRepo.findByUsuarioId(1L)).thenReturn(Arrays.asList(auditoria));
		when(mapper.map(auditoria, AuditoriaDTO.class)).thenReturn(dto);

		List<AuditoriaDTO> result = auditoriaService.obtenerPorUsuario(1L);

		assertTrue(result != null && result.size() == 1);
	}

	@Test(expected = RegistroNoEncontradoException.class)
	public void testObtenerPorUsuario_idInvalido_lanzaException() throws RegistroNoEncontradoException {
		auditoriaService.obtenerPorUsuario(0L);
	}

	@Test
	public void testObtenerPorAccion_retornaLista() {
		Auditoria auditoria = new Auditoria();
		AuditoriaDTO dto = new AuditoriaDTO();

		when(auditoriaRepo.findByAccion("CREAR")).thenReturn(Arrays.asList(auditoria));
		when(mapper.map(auditoria, AuditoriaDTO.class)).thenReturn(dto);

		List<AuditoriaDTO> result = auditoriaService.obtenerPorAccion("CREAR");

		assertEquals(1, result.size());
	}

	@Test
	public void testObtenerPorEntidad_retornaLista() {
		Auditoria auditoria = new Auditoria();
		AuditoriaDTO dto = new AuditoriaDTO();

		when(auditoriaRepo.findByEntidad("Usuario")).thenReturn(Arrays.asList(auditoria));
		when(mapper.map(auditoria, AuditoriaDTO.class)).thenReturn(dto);

		List<AuditoriaDTO> result = auditoriaService.obtenerPorEntidad("Usuario");

		assertEquals(1, result.size());
	}

	@Test
	public void testObtenerPorRango_retornaLista() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(1);
		LocalDateTime fin = LocalDateTime.now();

		Auditoria auditoria = new Auditoria();
		AuditoriaDTO dto = new AuditoriaDTO();

		when(auditoriaRepo.findByFechaAccionBetween(inicio, fin)).thenReturn(Arrays.asList(auditoria));
		when(mapper.map(auditoria, AuditoriaDTO.class)).thenReturn(dto);

		List<AuditoriaDTO> result = auditoriaService.obtenerPorRango(inicio, fin);

		assertEquals(1, result.size());
	}

	@After
	public void despues() throws Exception {
		closeable.close();
		System.out.println("Fin prueba: " + contar);
	}

	@AfterClass
	public static void despuesTodo() {
		System.out.println("Fin pruebas unitarias AuditoriaService");
	}
}