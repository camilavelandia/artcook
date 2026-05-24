package co.edu.unbosque.artcook.exception;

/**
 * Excepción que se lanza cuando las credenciales ingresadas en el login son incorrectas.
 */
public class CredencialesInvalidasException extends Exception {

    /** Identificador de versión para la serialización de la clase. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje genérico de credenciales incorrectas.
     */
    public CredencialesInvalidasException() {
        super("Correo o contrasena incorrectos.");
    }

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param mensaje descripción específica del error de credenciales
     */
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}