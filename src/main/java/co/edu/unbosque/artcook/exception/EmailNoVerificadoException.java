package co.edu.unbosque.artcook.exception;

/**
 * Excepción cuando el usuario intenta acceder sin haber verificado su correo.
 */
public class EmailNoVerificadoException extends Exception {
    private static final long serialVersionUID = 1L;

    public EmailNoVerificadoException() {
        super("Debe verificar su correo electronico antes de continuar.");
    }

    public EmailNoVerificadoException(String mensaje) {
        super(mensaje);
    }
}