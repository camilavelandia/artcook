package co.edu.unbosque.artcook.exception;

/**
 * Excepción cuando las credenciales de login son incorrectas.
 */
public class CredencialesInvalidasException extends Exception {
    private static final long serialVersionUID = 1L;

    public CredencialesInvalidasException() {
        super("Correo o contrasena incorrectos.");
    }

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
