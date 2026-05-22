package co.edu.unbosque.artcook.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado del envío de correos electrónicos del sistema.
 * Gestiona verificación de cuenta, recuperación de contraseña y notificaciones.
 */
@Service
public class CorreoService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String remitente;

    /**
     * Constructor por defecto.
     */
    public CorreoService() {
    }

    /**
     * Envía el correo de verificación de cuenta al usuario recién registrado.
     *
     * @param email  correo electrónico del destinatario
     * @param nombre nombre del usuario
     * @param token  token único de verificación
     */
    public void enviarCorreoVerificacion(String email, String nombre, String token) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject("Verificacion de cuenta - ArtCook");
        mensaje.setText(
            "Hola " + nombre + ",\n\n"
            + "Gracias por registrarte en ArtCook.\n\n"
            + "Para activar tu cuenta, haz clic en el siguiente enlace:\n"
            + baseUrl + "/verificar?token=" + token + "\n\n"
            + "Si no te registraste en nuestra plataforma, ignora este correo.\n\n"
            + "Equipo ArtCook"
        );
        mailSender.send(mensaje);
    }

    /**
     * Envía un correo de recuperación de contraseña.
     *
     * @param email  correo electrónico del destinatario
     * @param nombre nombre del usuario
     * @param token  token único de recuperación
     */
    public void enviarCorreoRecuperacion(String email, String nombre, String token) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject("Recuperacion de contrasena - ArtCook");
        mensaje.setText(
            "Hola " + nombre + ",\n\n"
            + "Para recuperar tu contrasena, haz clic en el siguiente enlace:\n"
            + baseUrl + "/usuario/cambiarcontrasena?token=" + token + "\n\n"
            + "Si no solicitaste este correo, ignoralo.\n\n"
            + "Equipo ArtCook"
        );
        mailSender.send(mensaje);
    }

    /**
     * Envía un correo de bienvenida cuando el usuario verifica exitosamente su cuenta.
     *
     * @param email  correo electrónico del destinatario
     * @param nombre nombre del usuario
     */
    public void enviarCorreoBienvenida(String email, String nombre) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject("Bienvenido a ArtCook");
        mensaje.setText(
            "Hola " + nombre + ",\n\n"
            + "Tu cuenta ha sido verificada exitosamente.\n\n"
            + "Ya puedes iniciar sesion y comenzar a generar recetas con inteligencia artificial.\n\n"
            + "Equipo ArtCook"
        );
        mailSender.send(mensaje);
    }

    /**
     * Envía una notificación cuando la cuenta del usuario es desactivada por el administrador.
     *
     * @param email  correo electrónico del destinatario
     * @param nombre nombre del usuario
     */
    public void enviarCorreoDesactivacion(String email, String nombre) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject("Cuenta desactivada - ArtCook");
        mensaje.setText(
            "Hola " + nombre + ",\n\n"
            + "Tu cuenta ha sido desactivada por el administrador.\n\n"
            + "Si crees que esto es un error, contacta al soporte.\n\n"
            + "Equipo ArtCook"
        );
        mailSender.send(mensaje);
    }

    /**
     * Envía una notificación genérica.
     *
     * @param email  correo electrónico del destinatario
     * @param asunto asunto del correo
     * @param cuerpo cuerpo del correo
     */
    public void enviarNotificacion(String email, String asunto, String cuerpo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        mailSender.send(mensaje);
    }
}
