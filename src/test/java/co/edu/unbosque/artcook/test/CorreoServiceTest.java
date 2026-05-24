package co.edu.unbosque.artcook.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import co.edu.unbosque.artcook.services.CorreoService;

@RunWith(MockitoJUnitRunner.class)
public class CorreoServiceTest {

	@Mock
	private JavaMailSender mailSender;

	@InjectMocks
	private CorreoService correoService;

	public static int contar = 0;
	private AutoCloseable closeable;

	@BeforeClass
	public static void antesTodo() {
		System.out.println("Inicio pruebas unitarias CorreoService");
	}

	@Before
	public void antes() throws Exception {
		contar++;
		System.out.println("Inicio prueba: " + contar);
		closeable = MockitoAnnotations.openMocks(this);
		setCampo(correoService, "baseUrl", "http://localhost:8081");
		setCampo(correoService, "remitente", "artcook@test.com");
	}

	private void setCampo(Object objeto, String nombreCampo, Object valor) throws Exception {
		Field campo = objeto.getClass().getDeclaredField(nombreCampo);
		campo.setAccessible(true);
		campo.set(objeto, valor);
	}

	@Test
	public void testEnviarCorreoVerificacion_enviaMensaje() {
		correoService.enviarCorreoVerificacion("user@test.com", "Juan", "abc123");

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());

		SimpleMailMessage mensaje = captor.getValue();

		assertEquals("artcook@test.com", mensaje.getFrom());
		assertEquals("user@test.com", mensaje.getTo()[0]);
		assertEquals("Verificacion de cuenta - ArtCook", mensaje.getSubject());
		assertTrue(mensaje.getText().contains("http://localhost:8081/verificar?token=abc123"));
	}

	@Test
	public void testEnviarCorreoRecuperacion_enviaMensaje() {
		correoService.enviarCorreoRecuperacion("user@test.com", "Juan", "tok123");

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());

		SimpleMailMessage mensaje = captor.getValue();

		assertEquals("Recuperacion de contrasena - ArtCook", mensaje.getSubject());
		assertTrue(mensaje.getText().contains("http://localhost:8081/usuario/cambiarcontrasena?token=tok123"));
	}

	@Test
	public void testEnviarCorreoBienvenida_enviaMensaje() {
		correoService.enviarCorreoBienvenida("user@test.com", "Juan");

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());

		assertEquals("Bienvenido a ArtCook", captor.getValue().getSubject());
	}

	@Test
	public void testEnviarCorreoDesactivacion_enviaMensaje() {
		correoService.enviarCorreoDesactivacion("user@test.com", "Juan");

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());

		assertEquals("Cuenta desactivada - ArtCook", captor.getValue().getSubject());
	}

	@Test
	public void testEnviarNotificacion_enviaMensajeGenerico() {
		correoService.enviarNotificacion("user@test.com", "Asunto prueba", "Cuerpo prueba");

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());

		SimpleMailMessage mensaje = captor.getValue();

		assertEquals("user@test.com", mensaje.getTo()[0]);
		assertEquals("Asunto prueba", mensaje.getSubject());
		assertEquals("Cuerpo prueba", mensaje.getText());
	}

	@After
	public void despues() throws Exception {
		closeable.close();
		System.out.println("Fin prueba: " + contar);
	}

	@AfterClass
	public static void despuesTodo() {
		System.out.println("Fin pruebas unitarias CorreoService");
	}
}
