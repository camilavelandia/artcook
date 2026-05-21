package co.edu.unbosque.artcook.exception;

import java.util.regex.Pattern;
import co.edu.unbosque.artcook.dto.TipoRecetaDTO;

/**
 * Clase centralizada que contiene todos los métodos de validación del sistema.
 * Todos los servicios delegan sus validaciones a esta clase.
 * Los métodos son estáticos porque no necesitan estado propio.
 */
public class LanzadorExcepciones {

    private static final String PATRON_EMAIL = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern pattern = Pattern.compile(PATRON_EMAIL);

    /**
     * Verifica que un campo no sea nulo ni vacío.
     *
     * @param valor      valor a validar
     * @param nombreCampo nombre del campo para el mensaje de error
     * @throws CampoVacioException si el campo es nulo o vacío
     */
    public static void verificarCampoVacio(String valor, String nombreCampo) throws CampoVacioException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new CampoVacioException(nombreCampo);
        }
    }

    /**
     * Alias de verificarCampoVacio para compatibilidad con código de Camila-Juan.
     *
     * @param campo       valor a validar
     * @param nombreCampo nombre del campo
     * @throws CampoVacioException si el campo es nulo o vacío
     */
    public static void validarCampoVacio(String campo, String nombreCampo) throws CampoVacioException {
        verificarCampoVacio(campo, nombreCampo);
    }

    /**
     * Verifica que el formato del email sea válido.
     *
     * @param email email a validar
     * @throws EmailFormatoException si el formato no es válido
     */
    public static void verificarFormatoEmail(String email) throws EmailFormatoException {
        if (email == null || !pattern.matcher(email).matches()) {
            throw new EmailFormatoException();
        }
    }

    /**
     * Alias de verificarFormatoEmail para compatibilidad con código de Camila-Juan.
     *
     * @param email email a validar
     * @throws EmailFormatoException si el formato no es válido
     */
    public static void validarEmail(String email) throws EmailFormatoException {
        verificarFormatoEmail(email);
    }

    /**
     * Verifica que el email no esté ya registrado en el sistema.
     *
     * @param existe true si el email ya existe en la base de datos
     * @throws EmailDuplicadoException si el email ya está registrado
     */
    public static void verificarEmailDuplicado(boolean existe) throws EmailDuplicadoException {
        if (existe) {
            throw new EmailDuplicadoException();
        }
    }

    /**
     * Verifica que la contraseña cumpla los requisitos mínimos.
     * Debe tener al menos 8 caracteres, un número y un carácter especial.
     *
     * @param contrasena contraseña a validar
     * @throws ContrasenaException si no cumple los requisitos
     */
    public static void verificarContrasena(String contrasena) throws ContrasenaException {
        if (contrasena == null || contrasena.length() < 8) {
            throw new ContrasenaException();
        }
        boolean tieneNumero = contrasena.matches(".*\\d.*");
        boolean tieneEspecial = contrasena.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?].*");
        if (!tieneNumero || !tieneEspecial) {
            throw new ContrasenaException();
        }
    }

    /**
     * Versión flexible de verificarContrasena que solo exige longitud mínima de 6.
     * Usada en flujos donde se permite contraseña simple.
     *
     * @param contrasena contraseña a validar
     * @throws ContrasenaException si no cumple los requisitos
     */
    public static void validarContrasena(String contrasena) throws ContrasenaException {
        if (contrasena == null || contrasena.isEmpty()) {
            throw new ContrasenaException("La contrasena no puede estar vacia.");
        }
        if (contrasena.length() < 6) {
            throw new ContrasenaException("La contrasena debe tener al menos 6 caracteres.");
        }
        if (contrasena.length() > 255) {
            throw new ContrasenaException("La contrasena no puede exceder 255 caracteres.");
        }
    }

    /**
     * Verifica que el nombre cumpla los requisitos mínimos.
     * Debe tener al menos 3 caracteres y solo letras con espacios.
     *
     * @param nombre nombre a validar
     * @throws NombreException si no cumple los requisitos
     */
    public static void verificarNombre(String nombre) throws NombreException {
        if (nombre == null || nombre.trim().length() < 3) {
            throw new NombreException();
        }
        if (!nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            throw new NombreException();
        }
        if (nombre.length() > 100) {
            throw new NombreException("El nombre no puede exceder 100 caracteres.");
        }
    }

    /**
     * Alias de verificarNombre para compatibilidad con código de Camila-Juan.
     *
     * @param nombre nombre a validar
     * @throws NombreException si no cumple los requisitos
     */
    public static void validarNombre(String nombre) throws NombreException {
        verificarNombre(nombre);
    }

    /**
     * Verifica que el prompt para generar la receta no esté vacío
     * y tenga una longitud mínima descriptiva.
     *
     * @param prompt prompt a validar
     * @throws PromptVacioException si el prompt es nulo, vacío o muy corto
     */
    public static void verificarPrompt(String prompt) throws PromptVacioException {
        if (prompt == null || prompt.trim().length() < 5) {
            throw new PromptVacioException();
        }
        if (prompt.length() > 500) {
            throw new PromptVacioException("El prompt no puede exceder 500 caracteres.");
        }
    }

    /**
     * Alias de verificarPrompt con validación mínima de 10 caracteres.
     *
     * @param prompt prompt a validar
     * @throws PromptVacioException si el prompt es nulo, vacío o muy corto
     */
    public static void validarPrompt(String prompt) throws PromptVacioException {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new PromptVacioException("El prompt no puede estar vacio.");
        }
        if (prompt.length() < 10) {
            throw new PromptVacioException("El prompt debe tener al menos 10 caracteres.");
        }
        if (prompt.length() > 500) {
            throw new PromptVacioException("El prompt no puede exceder 500 caracteres.");
        }
    }

    /**
     * Verifica que el tipo de receta no sea nulo (recibe TipoRecetaDTO).
     *
     * @param tipo tipo de receta a validar
     * @throws TipoRecetaException si el tipo es nulo
     */
    public static void verificarTipoReceta(TipoRecetaDTO tipo) throws TipoRecetaException {
        if (tipo == null) {
            throw new TipoRecetaException();
        }
    }

    /**
     * Verifica que el tipo de receta sea válido como String.
     *
     * @param tipoReceta tipo de receta como texto
     * @throws TipoRecetaException si el valor no es COCINA ni MANUALIDAD
     */
    public static void validarTipoReceta(String tipoReceta) throws TipoRecetaException {
        if (tipoReceta == null || tipoReceta.trim().isEmpty()) {
            throw new TipoRecetaException("El tipo de receta no puede estar vacio.");
        }
        if (!tipoReceta.equalsIgnoreCase("COCINA") && !tipoReceta.equalsIgnoreCase("MANUALIDAD")) {
            throw new TipoRecetaException("El tipo de receta debe ser COCINA o MANUALIDAD.");
        }
    }

    /**
     * Verifica que las porciones sean un número positivo mayor a cero.
     *
     * @param porciones número de porciones a validar
     * @throws CampoVacioException si las porciones son nulas o menores a 1
     */
    public static void validarPorciones(Integer porciones) throws CampoVacioException {
        if (porciones == null || porciones <= 0) {
            throw new CampoVacioException("Las porciones deben ser mayor a 0.");
        }
    }

    /**
     * Verifica que el ID sea un valor positivo válido.
     *
     * @param id identificador a validar
     * @throws RegistroNoEncontradoException si el ID es nulo o menor a 1
     */
    public static void validarId(Long id) throws RegistroNoEncontradoException {
        if (id == null || id <= 0) {
            throw new RegistroNoEncontradoException("El ID debe ser valido y mayor a 0.");
        }
    }

    /**
     * Verifica que el token de verificación sea válido.
     *
     * @param tokenEncontrado true si el token existe en la base de datos
     * @throws TokenInvalidoException si el token no existe
     */
    public static void verificarToken(boolean tokenEncontrado) throws TokenInvalidoException {
        if (!tokenEncontrado) {
            throw new TokenInvalidoException();
        }
    }

    /**
     * Alias de verificarToken para compatibilidad con código de Camila-Juan.
     *
     * @param token string del token a validar
     * @throws TokenInvalidoException si el token es nulo o vacío
     */
    public static void validarToken(String token) throws TokenInvalidoException {
        if (token == null || token.trim().isEmpty()) {
            throw new TokenInvalidoException("El token no puede estar vacio.");
        }
    }

    /**
     * Verifica que el email del usuario esté verificado antes de permitir el acceso.
     *
     * @param emailVerificado true si el email ya fue verificado
     * @throws EmailNoVerificadoException si el email no ha sido verificado
     */
    public static void verificarEmailVerificado(boolean emailVerificado) throws EmailNoVerificadoException {
        if (!emailVerificado) {
            throw new EmailNoVerificadoException();
        }
    }

    /**
     * Alias de verificarEmailVerificado para compatibilidad con código de Camila-Juan.
     *
     * @param emailVerificado true si el email ya fue verificado
     * @throws EmailNoVerificadoException si el email no ha sido verificado
     */
    public static void validarEmailVerificado(boolean emailVerificado) throws EmailNoVerificadoException {
        verificarEmailVerificado(emailVerificado);
    }

    /**
     * Verifica que el usuario esté activo en el sistema.
     *
     * @param activo true si el usuario está activo
     * @throws UsuarioInactivoException si el usuario está desactivado
     */
    public static void verificarUsuarioActivo(boolean activo) throws UsuarioInactivoException {
        if (!activo) {
            throw new UsuarioInactivoException();
        }
    }

    /**
     * Alias de verificarUsuarioActivo para compatibilidad con código de Camila-Juan.
     *
     * @param activo true si el usuario está activo
     * @throws UsuarioInactivoException si el usuario está desactivado
     */
    public static void validarUsuarioActivo(boolean activo) throws UsuarioInactivoException {
        verificarUsuarioActivo(activo);
    }

    /**
     * Verifica que las credenciales de login sean correctas.
     *
     * @param credencialesCorrectas true si el email y contraseña coinciden
     * @throws CredencialesInvalidasException si las credenciales son incorrectas
     */
    public static void verificarCredenciales(boolean credencialesCorrectas) throws CredencialesInvalidasException {
        if (!credencialesCorrectas) {
            throw new CredencialesInvalidasException();
        }
    }

    /**
     * Verifica que un registro exista en la base de datos por su ID.
     *
     * @param existe  true si el registro existe
     * @param entidad nombre de la entidad para el mensaje de error
     * @param id      identificador del registro
     * @throws RegistroNoEncontradoException si el registro no existe
     */
    public static void verificarExistencia(boolean existe, String entidad, Long id)
            throws RegistroNoEncontradoException {
        if (!existe) {
            throw new RegistroNoEncontradoException(entidad, id);
        }
    }

    /**
     * Alias de verificarExistencia simplificado para compatibilidad con código de Camila-Juan.
     *
     * @param existe true si el registro existe
     * @throws RegistroNoEncontradoException si el registro no existe
     */
    public static void validarRegistroExiste(boolean existe) throws RegistroNoEncontradoException {
        if (!existe) {
            throw new RegistroNoEncontradoException("El registro no fue encontrado.");
        }
    }
}