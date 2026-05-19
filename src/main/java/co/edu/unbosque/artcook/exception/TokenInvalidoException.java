package co.edu.unbosque.artcook.exception;

/**
 * Excepción cuando el token de verificación es inválido o ya fue utilizado.
 */
public class TokenInvalidoException extends Exception {
    private static final long serialVersionUID = 1L;

    public TokenInvalidoException() {
        super("El token de verificacion es invalido o ya expiro.");
    }

    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}
