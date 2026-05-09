package co.edu.unbosque.artcook.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.artcook.dto.LoginResponseDTO;
import co.edu.unbosque.artcook.dto.RolUsuarioDTO;
import co.edu.unbosque.artcook.dto.UsuarioDTO;
import co.edu.unbosque.artcook.exception.CredencialesInvalidasException;
import co.edu.unbosque.artcook.exception.EmailFormatoException;
import co.edu.unbosque.artcook.exception.EmailNoVerificadoException;
import co.edu.unbosque.artcook.exception.RegistroNoEncontradoException;
import co.edu.unbosque.artcook.exception.UsuarioInactivoException;
import co.edu.unbosque.artcook.services.AuditoriaService;
import co.edu.unbosque.artcook.services.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/usuario")
@CrossOrigin(origins = { "http://localhost:8081", "*" })
public class UsuarioController {

	@Autowired
	private UsuarioService usuarioSer;

	@Autowired
	private AuditoriaService auditoriaService;

	@PostMapping("/registrar")
	public ResponseEntity<String> registrar(@RequestParam String nombre, @RequestParam String email,
			@RequestParam String contrasena, HttpServletRequest request) {

		UsuarioDTO nuevo = new UsuarioDTO(nombre, email, contrasena, RolUsuarioDTO.USER);

		int resultado;
		try {
			resultado = usuarioSer.create(nuevo);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		switch (resultado) {
		case 0:
			auditoriaService.registrarAccion(nuevo.getId(), "REGISTRO", "Usuario",
					nuevo.getId(), "Nuevo usuario registrado: " + email);
			return new ResponseEntity<>("Registro exitoso. Revisa tu correo para verificar tu cuenta.",
					HttpStatus.CREATED);
		case 1:
			return new ResponseEntity<>("El nombre debe tener minimo 3 caracteres.", HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("El formato del correo no es valido.", HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("El correo ya esta registrado.", HttpStatus.CONFLICT);
		case 4:
			return new ResponseEntity<>("La contraseña debe tener minimo 6 caracteres.", HttpStatus.BAD_REQUEST);
		case 5:
			return new ResponseEntity<>("Error al enviar el correo de verificacion. Intente de nuevo.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		default:
			return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/verificar")
	public ResponseEntity<String> verificarCorreo(@RequestParam String token, HttpServletRequest request) {
		int resultado = usuarioSer.verificarEmail(token);
		switch (resultado) {
		case 0:
			auditoriaService.registrarAccion(0L, "VERIFICAR_EMAIL", "Usuario",
					null, "Email verificado exitosamente");
			return new ResponseEntity<>("Correo verificado exitosamente. Ya puedes iniciar sesion.",
					HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("Token de verificacion invalido o ya utilizado.", HttpStatus.BAD_REQUEST);
		default:
			return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestParam String email, @RequestParam String contrasena,
			HttpServletRequest request) {
		try {
			LoginResponseDTO response = usuarioSer.login(email, contrasena);
			auditoriaService.registrarAccion(response.getId(), "LOGIN", "Usuario",
					response.getId(), "Login exitoso");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (EmailFormatoException e) {
			return new ResponseEntity<>("Formato de correo invalido.", HttpStatus.BAD_REQUEST);
		} catch (CredencialesInvalidasException e) {
			return new ResponseEntity<>("Correo o contraseña incorrectos.", HttpStatus.UNAUTHORIZED);
		} catch (EmailNoVerificadoException e) {
			return new ResponseEntity<>("Debe verificar su correo antes de iniciar sesion.", HttpStatus.FORBIDDEN);
		} catch (UsuarioInactivoException e) {
			return new ResponseEntity<>("Su cuenta ha sido desactivada. Contacte al administrador.", HttpStatus.FORBIDDEN);
		}
	}

	@GetMapping("/mostrartodo")
	public ResponseEntity<List<UsuarioDTO>> obtenerTodo() {
		List<UsuarioDTO> lista = usuarioSer.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
	}

	@GetMapping("/poremail")
	public ResponseEntity<UsuarioDTO> obtenerPorEmail(@RequestParam String email) {
		try {
			UsuarioDTO usuario = usuarioSer.obtenerPorEmail(email);
			return new ResponseEntity<>(usuario, HttpStatus.OK);
		} catch (EmailFormatoException | RegistroNoEncontradoException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PutMapping("/actualizar")
	public ResponseEntity<String> actualizar(@RequestParam Long id, @RequestParam String nombre,
			@RequestParam boolean activo, HttpServletRequest request) {
		UsuarioDTO dto = new UsuarioDTO();
		dto.setNombre(nombre);
		dto.setActivo(activo);

		int resultado;
		try {
			resultado = usuarioSer.updateById(id, dto);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		switch (resultado) {
		case 0:
			auditoriaService.registrarAccion(id, "ACTUALIZAR_USUARIO", "Usuario",
					id, "Usuario ID " + id + " actualizado");
			return new ResponseEntity<>("Usuario actualizado exitosamente.", HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("Usuario no encontrado.", HttpStatus.NOT_FOUND);
		case 2:
			return new ResponseEntity<>("El nombre no es valido.", HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("El formato del correo no es valido.", HttpStatus.BAD_REQUEST);
		default:
			return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/eliminar")
	public ResponseEntity<String> eliminar(@RequestParam Long id, HttpServletRequest request) {
		int resultado;
		try {
			resultado = usuarioSer.deleteById(id);
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		switch (resultado) {
		case 0:
			auditoriaService.registrarAccion(id, "ELIMINAR_USUARIO", "Usuario",
					id, "Usuario ID " + id + " eliminado");
			return new ResponseEntity<>("Usuario eliminado exitosamente.", HttpStatus.ACCEPTED);
		case 1:
			return new ResponseEntity<>("Usuario no encontrado.", HttpStatus.NOT_FOUND);
		default:
			return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/estado")
	public ResponseEntity<String> cambiarEstado(@RequestParam Long id, @RequestParam boolean activo,
			HttpServletRequest request) {
		try {
			int resultado = activo ? usuarioSer.activarUsuario(id) : usuarioSer.desactivarUsuario(id);
			switch (resultado) {
			case 0:
				String accion = activo ? "ACTIVAR_USUARIO" : "DESACTIVAR_USUARIO";
				auditoriaService.registrarAccion(id, accion, "Usuario",
						id, "Usuario ID " + id + (activo ? " activado" : " desactivado"));
				return new ResponseEntity<>("Estado del usuario actualizado.", HttpStatus.ACCEPTED);
			case 1:
				return new ResponseEntity<>("Usuario no encontrado.", HttpStatus.NOT_FOUND);
			default:
				return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			return new ResponseEntity<>("Error inesperado", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/contar")
	public ResponseEntity<Long> contar() {
		return new ResponseEntity<>(usuarioSer.count(), HttpStatus.OK);
	}

	@GetMapping("/existe")
	public ResponseEntity<Boolean> existe(@RequestParam Long id) {
		return new ResponseEntity<>(usuarioSer.exist(id), HttpStatus.OK);
	}

	@PostMapping("/recuperar")
	public ResponseEntity<String> solicitarRecuperacion(@RequestParam String email,
			HttpServletRequest request) {
		int resultado = usuarioSer.solicitarRecuperacion(email);
		switch (resultado) {
		case 0:
			auditoriaService.registrarAccion(0L, "SOLICITAR_RECUPERACION", "Usuario",
					null, "Solicitud de recuperacion para: " + email);
			return new ResponseEntity<>("Se envio un correo con las instrucciones para recuperar tu contrasena.",
					HttpStatus.OK);
		case 1:
			return new ResponseEntity<>("El correo no esta registrado.", HttpStatus.NOT_FOUND);
		case 2:
			return new ResponseEntity<>("El formato del correo no es valido.", HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("Error al enviar el correo. Intente de nuevo.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		default:
			return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/cambiarcontrasena")
	public ResponseEntity<String> cambiarContrasena(@RequestParam String token,
			@RequestParam String nuevaContrasena, HttpServletRequest request) {
		int resultado = usuarioSer.cambiarContrasena(token, nuevaContrasena);
		switch (resultado) {
		case 0:
			auditoriaService.registrarAccion(0L, "CAMBIAR_CONTRASENA", "Usuario",
					null, "Contrasena cambiada exitosamente");
			return new ResponseEntity<>("Contrasena cambiada exitosamente. Ya puedes iniciar sesion.",
					HttpStatus.OK);
		case 1:
			return new ResponseEntity<>("El token es invalido o ya fue utilizado.", HttpStatus.BAD_REQUEST);
		case 2:
			return new ResponseEntity<>("El token no es valido.", HttpStatus.BAD_REQUEST);
		case 3:
			return new ResponseEntity<>("La contrasena debe tener minimo 6 caracteres.", HttpStatus.BAD_REQUEST);
		case 4:
			return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
		default:
			return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}