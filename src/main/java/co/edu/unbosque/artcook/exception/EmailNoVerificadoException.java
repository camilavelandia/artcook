package co.edu.unbosque.artcook.exception;

/**
 * Excepción que se lanza cuando el usuario intenta acceder sin haber verificado su correo electrónico.
 */
public class EmailNoVerificadoException extends Exception {

    /** Identificador de versión para la serialización de la clase. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje genérico de correo no verificado.
     */
    public EmailNoVerificadoException() {
        super("Debe verificar su correo electronico antes de continuar.");
    }

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param mensaje descripción específica del error de verificación de correo
     */
    public EmailNoVerificadoException(String mensaje) {
        super(mensaje);
    }
}