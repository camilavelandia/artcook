package co.edu.unbosque.artcook.exception;

/**
 * Excepción que se lanza cuando la contraseña no cumple los requisitos mínimos de seguridad.
 */
public class ContrasenaException extends Exception {

    /** Identificador de versión para la serialización de la clase. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje genérico sobre los requisitos de contraseña.
     */
    public ContrasenaException() {
        super("La contrasena debe tener minimo 8 caracteres, al menos un numero y un caracter especial.");
    }

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param mensaje descripción específica del error de contraseña
     */
    public ContrasenaException(String mensaje) {
        super(mensaje);
    }
}