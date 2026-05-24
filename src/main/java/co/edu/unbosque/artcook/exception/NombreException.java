package co.edu.unbosque.artcook.exception;

/**
 * Excepción que se lanza cuando el nombre ingresado no cumple los requisitos mínimos.
 */
public class NombreException extends Exception {

    /** Identificador de versión para la serialización de la clase. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje genérico sobre los requisitos del nombre.
     */
    public NombreException() {
        super("El nombre debe tener minimo 3 caracteres y solo puede contener letras y espacios.");
    }

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param mensaje descripción específica del error de nombre
     */
    public NombreException(String mensaje) {
        super(mensaje);
    }
}