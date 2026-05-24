package co.edu.unbosque.artcook.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import co.edu.unbosque.artcook.dto.LoginResponseDTO;
import co.edu.unbosque.artcook.dto.RolUsuarioDTO;
import co.edu.unbosque.artcook.dto.UsuarioDTO;
import co.edu.unbosque.artcook.entity.RolUsuario;
import co.edu.unbosque.artcook.entity.Usuario;
import co.edu.unbosque.artcook.exception.CredencialesInvalidasException;
import co.edu.unbosque.artcook.exception.EmailFormatoException;
import co.edu.unbosque.artcook.exception.EmailNoVerificadoException;
import co.edu.unbosque.artcook.exception.RegistroNoEncontradoException;
import co.edu.unbosque.artcook.exception.UsuarioInactivoException;
import co.edu.unbosque.artcook.repository.UsuarioRepository;
import co.edu.unbosque.artcook.services.CorreoService;
import co.edu.unbosque.artcook.services.UsuarioService;

@RunWith(MockitoJUnitRunner.class)
public class UsuarioServiceTest {

	@Mock
	private UsuarioRepository usuarioRepo;

	@Mock
	private ModelMapper mapper;

	@Mock
	private CorreoService correoService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UsuarioService usuarioService;

	public static int contar = 0;
	private AutoCloseable closeable;

	@BeforeClass
	public static void antesTodo() {
		System.out.println("Inicio pruebas unitarias UsuarioService");
	}

	@Before
	public void antes() {
		contar++;
		System.out.println("Inicio prueba: " + contar);
		closeable = MockitoAnnotations.openMocks(this);
	}

	private UsuarioDTO crearDTO() {
		UsuarioDTO dto = new UsuarioDTO();
		dto.setNombre("Juan Perez");
		dto.setEmail("juan@test.com");
		dto.setContrasena("Password123!");
		dto.setRol(RolUsuarioDTO.USER);
		dto.setActivo(true);
		return dto;
	}

	private Usuario crearEntidad() {
		Usuario usuario = new Usuario("Juan Perez", "juan@test.com", "hash", RolUsuario.USER);
		usuario.setId(1L);
		usuario.setActivo(true);
		usuario.setEmailVerificado(true);
		return usuario;
	}

	@Test
	public void testCreate_exitoso() {
		UsuarioDTO dto = crearDTO();

		when(usuarioRepo.existsByEmail(dto.getEmail())).thenReturn(false);
		when(passwordEncoder.encode(dto.getContrasena())).thenReturn("hash");
		when(usuarioRepo.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

		int result = usuarioService.create(dto);

		assertEquals(0, result);
		verify(correoService).enviarCorreoVerificacion(any(String.class), any(String.class), any(String.class));
	}

	@Test
	public void testCreate_nombreInvalido_retorna1() {
		UsuarioDTO dto = crearDTO();
		dto.setNombre("");

		int result = usuarioService.create(dto);

		assertEquals(1, result);
	}

	@Test
	public void testCreate_emailInvalido_retorna2() {
		UsuarioDTO dto = crearDTO();
		dto.setEmail("correo-malo");

		int result = usuarioService.create(dto);

		assertEquals(2, result);
	}

	@Test
	public void testCreate_emailDuplicado_retorna3() {
		UsuarioDTO dto = crearDTO();

		when(usuarioRepo.existsByEmail(dto.getEmail())).thenReturn(true);

		int result = usuarioService.create(dto);

		assertEquals(3, result);
	}

	@Test
	public void testCreate_contrasenaInvalida_retorna4() {
		UsuarioDTO dto = crearDTO();
		dto.setContrasena("123");

		when(usuarioRepo.existsByEmail(dto.getEmail())).thenReturn(false);

		int result = usuarioService.create(dto);

		assertEquals(4, result);
	}

	@Test
	public void testGetAll_retornaLista() {
		Usuario u1 = crearEntidad();
		Usuario u2 = crearEntidad();

		UsuarioDTO dto1 = new UsuarioDTO();
		UsuarioDTO dto2 = new UsuarioDTO();

		when(usuarioRepo.findAll()).thenReturn(Arrays.asList(u1, u2));
		when(mapper.map(u1, UsuarioDTO.class)).thenReturn(dto1);
		when(mapper.map(u2, UsuarioDTO.class)).thenReturn(dto2);

		List<UsuarioDTO> result = usuarioService.getAll();

		assertTrue(result != null && result.size() == 2);
	}

	@Test
	public void testDeleteByID_exitoso() {
		when(usuarioRepo.existsById(1L)).thenReturn(true);
		doNothing().when(usuarioRepo).deleteById(1L);

		int result = usuarioService.deleteByID(1L);

		assertEquals(0, result);
	}

	@Test
	public void testDeleteByID_noExiste_retorna1() {
		when(usuarioRepo.existsById(99L)).thenReturn(false);

		int result = usuarioService.deleteByID(99L);

		assertEquals(1, result);
	}

	@Test
	public void testUpdateByID_exitoso() {
		Usuario usuario = crearEntidad();
		UsuarioDTO dto = crearDTO();
		dto.setNombre("Nuevo Nombre");
		dto.setRol(RolUsuarioDTO.ADMIN);

		when(usuarioRepo.existsById(1L)).thenReturn(true);
		when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));
		when(usuarioRepo.save(usuario)).thenReturn(usuario);

		int result = usuarioService.updateByID(1L, dto);

		assertEquals(0, result);
		assertEquals("Nuevo Nombre", usuario.getNombre());
	}

	@Test
	public void testUpdateByID_noExiste_retorna1() {
		when(usuarioRepo.existsById(99L)).thenReturn(false);

		int result = usuarioService.updateByID(99L, crearDTO());

		assertEquals(1, result);
	}

	@Test
	public void testUpdateByID_nombreInvalido_retorna2() {
		UsuarioDTO dto = crearDTO();
		dto.setNombre("");

		when(usuarioRepo.existsById(1L)).thenReturn(true);

		int result = usuarioService.updateByID(1L, dto);

		assertEquals(2, result);
	}

	@Test
	public void testCount_retornaCantidad() {
		when(usuarioRepo.count()).thenReturn(7L);

		long result = usuarioService.count();

		assertEquals(7L, result);
	}

	@Test
	public void testExist_idExistente_retornaTrue() {
		when(usuarioRepo.existsById(1L)).thenReturn(true);

		boolean result = usuarioService.exist(1L);

		assertEquals(true, result);
	}

	@Test
	public void testLogin_exitoso() throws EmailFormatoException, CredencialesInvalidasException,
			EmailNoVerificadoException, UsuarioInactivoException {
		Usuario usuario = crearEntidad();

		when(usuarioRepo.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("Password123!", "hash")).thenReturn(true);
		when(usuarioRepo.save(usuario)).thenReturn(usuario);

		LoginResponseDTO result = usuarioService.login("juan@test.com", "Password123!");

		assertEquals("Juan Perez", result.getNombre());
		assertEquals("juan@test.com", result.getEmail());
	}

	@Test(expected = CredencialesInvalidasException.class)
	public void testLogin_credencialesInvalidas_lanzaException() throws EmailFormatoException,
			CredencialesInvalidasException, EmailNoVerificadoException, UsuarioInactivoException {
		when(usuarioRepo.findByEmail("juan@test.com")).thenReturn(Optional.empty());

		usuarioService.login("juan@test.com", "Password123!");
	}

	@Test(expected = EmailNoVerificadoException.class)
	public void testLogin_emailNoVerificado_lanzaException() throws EmailFormatoException,
			CredencialesInvalidasException, EmailNoVerificadoException, UsuarioInactivoException {
		Usuario usuario = crearEntidad();
		usuario.setEmailVerificado(false);

		when(usuarioRepo.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("Password123!", "hash")).thenReturn(true);

		usuarioService.login("juan@test.com", "Password123!");
	}

	@Test(expected = UsuarioInactivoException.class)
	public void testLogin_usuarioInactivo_lanzaException() throws EmailFormatoException,
			CredencialesInvalidasException, EmailNoVerificadoException, UsuarioInactivoException {
		Usuario usuario = crearEntidad();
		usuario.setActivo(false);

		when(usuarioRepo.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("Password123!", "hash")).thenReturn(true);

		usuarioService.login("juan@test.com", "Password123!");
	}

	@Test
	public void testVerificarEmail_exitoso() {
		Usuario usuario = crearEntidad();

		when(usuarioRepo.findByTokenVerificacion("token123")).thenReturn(Optional.of(usuario));
		when(usuarioRepo.save(usuario)).thenReturn(usuario);

		int result = usuarioService.verificarEmail("token123");

		assertEquals(0, result);
		verify(correoService).enviarCorreoBienvenida(usuario.getEmail(), usuario.getNombre());
	}

	@Test
	public void testVerificarEmail_tokenInvalido_retorna1() {
		when(usuarioRepo.findByTokenVerificacion("malo")).thenReturn(Optional.empty());

		int result = usuarioService.verificarEmail("malo");

		assertEquals(1, result);
	}

	@Test
	public void testObtenerPorEmail_exitoso() throws EmailFormatoException, RegistroNoEncontradoException {
		Usuario usuario = crearEntidad();
		UsuarioDTO dto = crearDTO();

		when(usuarioRepo.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
		when(mapper.map(usuario, UsuarioDTO.class)).thenReturn(dto);

		UsuarioDTO result = usuarioService.obtenerPorEmail("juan@test.com");

		assertEquals(dto, result);
	}

	@Test(expected = RegistroNoEncontradoException.class)
	public void testObtenerPorEmail_noExiste_lanzaException() throws EmailFormatoException, RegistroNoEncontradoException {
		when(usuarioRepo.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

		usuarioService.obtenerPorEmail("nadie@test.com");
	}

	@Test
	public void testGetByRol_retornaLista() {
		Usuario usuario = crearEntidad();
		UsuarioDTO dto = crearDTO();

		when(usuarioRepo.findByRol(RolUsuario.USER)).thenReturn(Arrays.asList(usuario));
		when(mapper.map(usuario, UsuarioDTO.class)).thenReturn(dto);

		List<UsuarioDTO> result = usuarioService.getByRol(RolUsuario.USER);

		assertEquals(1, result.size());
	}

	@Test
	public void testActivarUsuario_exitoso() {
		Usuario usuario = crearEntidad();
		usuario.setActivo(false);

		when(usuarioRepo.existsById(1L)).thenReturn(true);
		when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));
		when(usuarioRepo.save(usuario)).thenReturn(usuario);

		int result = usuarioService.activarUsuario(1L);

		assertEquals(0, result);
		assertEquals(true, usuario.isActivo());
	}

	@Test
	public void testActivarUsuario_noExiste_retorna1() {
		when(usuarioRepo.existsById(99L)).thenReturn(false);

		int result = usuarioService.activarUsuario(99L);

		assertEquals(1, result);
	}

	@Test
	public void testDesactivarUsuario_exitoso() {
		Usuario usuario = crearEntidad();

		when(usuarioRepo.existsById(1L)).thenReturn(true);
		when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));
		when(usuarioRepo.save(usuario)).thenReturn(usuario);

		int result = usuarioService.desactivarUsuario(1L);

		assertEquals(0, result);
		assertEquals(false, usuario.isActivo());
		verify(correoService).enviarCorreoDesactivacion(usuario.getEmail(), usuario.getNombre());
	}

	@Test
	public void testSolicitarRecuperacion_exitoso() {
		Usuario usuario = crearEntidad();

		when(usuarioRepo.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
		when(usuarioRepo.save(usuario)).thenReturn(usuario);

		int result = usuarioService.solicitarRecuperacion("juan@test.com");

		assertEquals(0, result);
		verify(correoService).enviarCorreoRecuperacion(any(String.class), any(String.class), any(String.class));
	}

	@Test
	public void testSolicitarRecuperacion_noExiste_retorna1() {
		when(usuarioRepo.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

		int result = usuarioService.solicitarRecuperacion("nadie@test.com");

		assertEquals(1, result);
	}

	@Test
	public void testSolicitarRecuperacion_emailInvalido_retorna2() {
		int result = usuarioService.solicitarRecuperacion("correo-malo");

		assertEquals(2, result);
	}

	@Test
	public void testCambiarContrasena_exitoso() {
		Usuario usuario = crearEntidad();

		when(usuarioRepo.findByTokenRecuperacion("token123")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.encode("NuevaPass123!")).thenReturn("nuevoHash");
		when(usuarioRepo.save(usuario)).thenReturn(usuario);

		int result = usuarioService.cambiarContrasena("token123", "NuevaPass123!");

		assertEquals(0, result);
		assertEquals("nuevoHash", usuario.getContrasena());
	}

	@Test
	public void testCambiarContrasena_tokenNoExiste_retorna1() {
		when(usuarioRepo.findByTokenRecuperacion("token123")).thenReturn(Optional.empty());

		int result = usuarioService.cambiarContrasena("token123", "NuevaPass123!");

		assertEquals(1, result);
	}

	@Test
	public void testCambiarContrasena_tokenVacio_retorna2() {
		int result = usuarioService.cambiarContrasena("", "NuevaPass123!");

		assertEquals(2, result);
	}

	@Test
	public void testCambiarContrasena_contrasenaInvalida_retorna3() {
		Usuario usuario = crearEntidad();

		when(usuarioRepo.findByTokenRecuperacion("token123")).thenReturn(Optional.of(usuario));

		int result = usuarioService.cambiarContrasena("token123", "123");

		assertEquals(3, result);
	}

	@After
	public void despues() throws Exception {
		closeable.close();
		System.out.println("Fin prueba: " + contar);
	}

	@AfterClass
	public static void despuesTodo() {
		System.out.println("Fin pruebas unitarias UsuarioService");
	}
}
