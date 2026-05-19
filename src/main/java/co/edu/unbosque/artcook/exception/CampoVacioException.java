package co.edu.unbosque.artcook.exception;

/**
 * Excepción cuando un campo requerido está vacío o nulo.
 */
public class CampoVacioException extends Exception {
    private static final long serialVersionUID = 1L;

    public CampoVacioException() {
        super("El campo no puede estar vacío.");
    }

    public CampoVacioException(String campo) {
        super("El campo '" + campo + "' no puede estar vacío.");
    }
}
