package co.edu.unbosque.artcook.exception;

/**
 * Excepción cuando el email ya está registrado en el sistema.
 */
public class EmailDuplicadoException extends Exception {
    private static final long serialVersionUID = 1L;

    public EmailDuplicadoException() {
        super("El email ya esta registrado.");
    }

    public EmailDuplicadoException(String mensaje) {
        super(mensaje);
    }
}