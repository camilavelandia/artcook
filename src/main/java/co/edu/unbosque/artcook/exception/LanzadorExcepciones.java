// clase modificada camila-juan

package co.edu.unbosque.artcook.exception;

import java.util.regex.Pattern;

public class LanzadorExcepciones {

	private static final String PATRON_EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$";
	private static final Pattern pattern = Pattern.compile(PATRON_EMAIL);

	public static void validarCampoVacio(String campo, String nombreCampo) throws CampoVacioException {
		if (campo == null || campo.trim().isEmpty()) {
			throw new CampoVacioException("El campo '" + nombreCampo + "' no puede estar vacío");
		}
	}

	public static void validarNombre(String nombre) throws NombreException {
		if (nombre == null || nombre.trim().isEmpty()) {
			throw new NombreException("El nombre no puede estar vacío");
		}
		if (nombre.length() < 3) {
			throw new NombreException("El nombre debe tener al menos 3 caracteres");
		}
		if (nombre.length() > 100) {
			throw new NombreException("El nombre no puede exceder 100 caracteres");
		}
	}

	public static void validarEmail(String email) throws EmailFormatoException {
		if (email == null || email.trim().isEmpty()) {
			throw new EmailFormatoException("El email no puede estar vacío");
		}
		if (!pattern.matcher(email).matches()) {
			throw new EmailFormatoException("El formato del email es inválido");
		}
	}

	public static void validarContrasena(String contrasena) throws ContrasenaException {
		if (contrasena == null || contrasena.isEmpty()) {
			throw new ContrasenaException("La contraseña no puede estar vacía");
		}
		if (contrasena.length() < 6) {
			throw new ContrasenaException("La contraseña debe tener al menos 6 caracteres");
		}
		if (contrasena.length() > 255) {
			throw new ContrasenaException("La contraseña no puede exceder 255 caracteres");
		}
	}

	public static void validarPrompt(String prompt) throws PromptVacioException {
		if (prompt == null || prompt.trim().isEmpty()) {
			throw new PromptVacioException("El prompt no puede estar vacío");
		}
		if (prompt.length() < 10) {
			throw new PromptVacioException("El prompt debe tener al menos 10 caracteres");
		}
		if (prompt.length() > 500) {
			throw new PromptVacioException("El prompt no puede exceder 500 caracteres");
		}
	}

	public static void validarTipoReceta(String tipoReceta) throws TipoRecetaException {
		if (tipoReceta == null || tipoReceta.trim().isEmpty()) {
			throw new TipoRecetaException("El tipo de receta no puede estar vacío");
		}
		if (!tipoReceta.equalsIgnoreCase("COCINA") && !tipoReceta.equalsIgnoreCase("MANUALIDAD")) {
			throw new TipoRecetaException("El tipo de receta debe ser COCINA o MANUALIDAD");
		}
	}

	public static void validarPorciones(Integer porciones) throws CampoVacioException {
		if (porciones == null || porciones <= 0) {
			throw new CampoVacioException("Las porciones deben ser mayor a 0");
		}
	}

	public static void validarId(Long id) throws RegistroNoEncontradoException {
		if (id == null || id <= 0) {
			throw new RegistroNoEncontradoException("El ID debe ser válido y mayor a 0");
		}
	}

	public static void validarToken(String token) throws TokenInvalidoException {
		if (token == null || token.trim().isEmpty()) {
			throw new TokenInvalidoException("El token no puede estar vacío");
		}
	}

	public static void validarRegistroExiste(boolean existe) throws RegistroNoEncontradoException {
		if (!existe) {
			throw new RegistroNoEncontradoException("El registro no fue encontrado");
		}
	}

	public static void validarUsuarioActivo(boolean activo) throws UsuarioInactivoException {
		if (!activo) {
			throw new UsuarioInactivoException("El usuario ha sido desactivado");
		}
	}

	public static void validarEmailVerificado(boolean emailVerificado) throws EmailNoVerificadoException {
		if (!emailVerificado) {
			throw new EmailNoVerificadoException("El email debe ser verificado para continuar");
		}
	}
}
