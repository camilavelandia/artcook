package co.edu.unbosque.artcook.exception;

/**
 * Excepción cuando el formato del email es inválido.
 */
public class EmailFormatoException extends Exception {
    private static final long serialVersionUID = 1L;

    public EmailFormatoException() {
        super("El formato del correo no es valido.");
    }

    public EmailFormatoException(String mensaje) {
        super(mensaje);
    }
}
