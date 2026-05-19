package co.edu.unbosque.artcook.exception;

/**
 * Excepción cuando el tipo de receta es inválido o nulo.
 */
public class TipoRecetaException extends Exception {
    private static final long serialVersionUID = 1L;

    public TipoRecetaException() {
        super("El tipo de receta debe ser COCINA o MANUALIDAD.");
    }

    public TipoRecetaException(String mensaje) {
        super(mensaje);
    }
}
