package co.edu.unbosque.artcook.exception;

/**
 * Excepción que se lanza cuando un campo requerido está vacío o nulo.
 */
public class CampoVacioException extends Exception {

    /** Identificador de versión para la serialización de la clase. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje genérico de campo vacío.
     */
    public CampoVacioException() {
        super("El campo no puede estar vacío.");
    }

    /**
     * Crea la excepción indicando el nombre del campo que está vacío.
     *
     * @param campo nombre del campo que no puede estar vacío
     */
    public CampoVacioException(String campo) {
        super("El campo '" + campo + "' no puede estar vacío.");
    }
}