package co.edu.unbosque.artcook.exception;

/**
 * Excepción cuando el usuario está desactivado por el administrador.
 */
public class UsuarioInactivoException extends Exception {
    private static final long serialVersionUID = 1L;

    public UsuarioInactivoException() {
        super("Su cuenta ha sido desactivada. Contacte al administrador.");
    }

    public UsuarioInactivoException(String mensaje) {
        super(mensaje);
    }
}
