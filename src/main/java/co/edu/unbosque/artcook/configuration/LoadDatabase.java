package co.edu.unbosque.artcook.configuration;

import co.edu.unbosque.artcook.entity.RolUsuario;
import co.edu.unbosque.artcook.entity.Usuario;
import co.edu.unbosque.artcook.repository.UsuarioRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase de configuración para cargar datos iniciales en la base de datos de
 * ArtCook. Crea un usuario administrador predeterminado al iniciar la
 * aplicación si no existe. Esto garantiza que siempre haya al menos un
 * administrador disponible.
 */
@Configuration
public class LoadDatabase {

	/** Logger para registrar mensajes durante la carga de datos iniciales. */
	private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

	@Value("${admin.email}")
	private String adminEmail;

	@Value("${admin.password}")
	private String adminPassword;

	@Value("${user.email}")
	private String userEmail;

	@Value("${user.password}")
	private String userPassword;

	/**
	 * Inicializa la base de datos con el usuario administrador predeterminado. Solo
	 * crea el usuario si no existe previamente para evitar duplicados.
	 *
	 * @param usuarioRepo     repositorio de usuarios para acceder a la base de
	 *                        datos
	 * @param passwordEncoder codificador de contraseñas para encriptar las
	 *                        contraseñas
	 * @return CommandLineRunner que se ejecuta al iniciar la aplicación
	 */
	@Bean
	CommandLineRunner initDatabase(UsuarioRepository usuarioRepo, PasswordEncoder passwordEncoder) {
		return args -> {

			Optional<Usuario> adminExistente = usuarioRepo.findByEmail(adminEmail);
			if (adminExistente.isPresent()) {
				log.info("El administrador ya existe, omitiendo creacion...");
			} else {
				Usuario admin = new Usuario("Administrador ArtCook", adminEmail, passwordEncoder.encode(adminPassword),
						RolUsuario.ADMIN);
				admin.setEmailVerificado(true);
				admin.setActivo(true);
				usuarioRepo.save(admin);
				log.info("Administrador creado: {}", adminEmail);
			}

			Optional<Usuario> userExistente = usuarioRepo.findByEmail(userEmail);
			if (userExistente.isPresent()) {
				log.info("El usuario de prueba ya existe, omitiendo creacion...");
			} else {
				Usuario usuarioPrueba = new Usuario("Usuario Prueba", userEmail, passwordEncoder.encode(userPassword),
						RolUsuario.USER);
				usuarioPrueba.setEmailVerificado(true);
				usuarioPrueba.setActivo(true);
				usuarioRepo.save(usuarioPrueba);
				log.info("Usuario de prueba creado: {}", userEmail);
			}
		};
	}
}