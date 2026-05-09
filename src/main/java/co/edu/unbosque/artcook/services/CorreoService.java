package co.edu.unbosque.artcook.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CorreoService {

	@Autowired
	private JavaMailSender mailSender;

	@Value("${app.base-url}")
	private String baseUrl;

	public void enviarTokenVerificacion(String email, String token) throws Exception {
		SimpleMailMessage mensaje = new SimpleMailMessage();
		mensaje.setTo(email);
		mensaje.setSubject("Verificacion de correo electronico - Artcook");
		mensaje.setText("Hola,\n\nPara verificar tu correo electronico, haz clic en el siguiente enlace:\n\n"
				+ baseUrl + "/verificar?token=" + token + "\n\n"
				+ "Si no solicitaste este correo, ignoralo.\n\n"
				+ "Saludos,\nEl equipo de Artcook");
		mailSender.send(mensaje);
	}

	public void enviarCorreoRecuperacion(String email, String token) throws Exception {
		SimpleMailMessage mensaje = new SimpleMailMessage();
		mensaje.setTo(email);
		mensaje.setSubject("Recuperacion de contrasena - Artcook");
		mensaje.setText("Hola,\n\nPara recuperar tu contrasena, haz clic en el siguiente enlace:\n\n"
				+ baseUrl + "/recuperar?token=" + token + "\n\n"
				+ "Si no solicitaste este correo, ignoralo.\n\n"
				+ "Saludos,\nEl equipo de Artcook");
		mailSender.send(mensaje);
	}

	public void enviarCorreoBienvenida(String email) throws Exception {
		SimpleMailMessage mensaje = new SimpleMailMessage();
		mensaje.setTo(email);
		mensaje.setSubject("Bienvenido a Artcook");
		mensaje.setText("Hola,\n\nTu cuenta ha sido verificada exitosamente.\n\n"
				+ "Ya puedes iniciar sesion y comenzar a generar recetas con inteligencia artificial.\n\n"
				+ "Saludos,\nEl equipo de Artcook");
		mailSender.send(mensaje);
	}

	public void enviarNotificacion(String email, String asunto, String cuerpo) throws Exception {
		SimpleMailMessage mensaje = new SimpleMailMessage();
		mensaje.setTo(email);
		mensaje.setSubject(asunto);
		mensaje.setText(cuerpo);
		mailSender.send(mensaje);
	}
}