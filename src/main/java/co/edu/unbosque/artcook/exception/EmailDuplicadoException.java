package co.edu.unbosque.artcook.exception;

/**
 * Excepción que se lanza cuando el email ingresado ya está registrado en el sistema.
 */
public class EmailDuplicadoException extends Exception {

    /** Identificador de versión para la serialización de la clase. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje genérico de email duplicado.
     */
    public EmailDuplicadoException() {
        super("El email ya esta registrado.");
    }

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param mensaje descripción específica del error de email duplicado
     */
    public EmailDuplicadoException(String mensaje) {
        super(mensaje);
    }
}