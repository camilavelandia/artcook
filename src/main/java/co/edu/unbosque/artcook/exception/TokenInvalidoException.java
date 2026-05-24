package co.edu.unbosque.artcook.exception;

/**
 * Excepción que se lanza cuando el token de verificación es inválido o ya fue utilizado.
 */
public class TokenInvalidoException extends Exception {

    /** Identificador de versión para la serialización de la clase. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje genérico de token inválido o expirado.
     */
    public TokenInvalidoException() {
        super("El token de verificacion es invalido o ya expiro.");
    }

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param mensaje descripción específica del error de token
     */
    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}