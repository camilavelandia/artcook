package co.edu.unbosque.artcook.exception;

/**
 * Excepción cuando el nombre no cumple los requisitos mínimos.
 */
public class NombreException extends Exception {
    private static final long serialVersionUID = 1L;

    public NombreException() {
        super("El nombre debe tener minimo 3 caracteres y solo puede contener letras y espacios.");
    }

    public NombreException(String mensaje) {
        super(mensaje);
    }
}