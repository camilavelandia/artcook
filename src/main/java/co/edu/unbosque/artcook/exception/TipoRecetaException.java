package co.edu.unbosque.artcook.exception;

/**
 * Excepción que se lanza cuando el tipo de receta ingresado es inválido o nulo.
 */
public class TipoRecetaException extends Exception {

    /** Identificador de versión para la serialización de la clase. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje genérico sobre los tipos de receta válidos.
     */
    public TipoRecetaException() {
        super("El tipo de receta debe ser COCINA o MANUALIDAD.");
    }

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param mensaje descripción específica del error de tipo de receta
     */
    public TipoRecetaException(String mensaje) {
        super(mensaje);
    }
}