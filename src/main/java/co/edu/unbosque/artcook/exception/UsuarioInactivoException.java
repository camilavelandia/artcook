package co.edu.unbosque.artcook.exception;

/**
 * Excepción que se lanza cuando el usuario está desactivado por el administrador.
 */
public class UsuarioInactivoException extends Exception {

    /** Identificador de versión para la serialización de la clase. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje genérico de cuenta desactivada.
     */
    public UsuarioInactivoException() {
        super("Su cuenta ha sido desactivada. Contacte al administrador.");
    }

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param mensaje descripción específica del error de usuario inactivo
     */
    public UsuarioInactivoException(String mensaje) {
        super(mensaje);
    }
}