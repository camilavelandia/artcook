package co.edu.unbosque.artcook.exception;

/**
 * Excepción que se lanza cuando el formato del email ingresado no es válido.
 */
public class EmailFormatoException extends Exception {

    /** Identificador de versión para la serialización de la clase. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje genérico de formato de correo inválido.
     */
    public EmailFormatoException() {
        super("El formato del correo no es valido.");
    }

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param mensaje descripción específica del error de formato de email
     */
    public EmailFormatoException(String mensaje) {
        super(mensaje);
    }
}