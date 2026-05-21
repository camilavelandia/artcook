package co.edu.unbosque.artcook.exception;

/**
 * Excepción cuando un registro no se encuentra en la base de datos.
 */
public class RegistroNoEncontradoException extends Exception {
    private static final long serialVersionUID = 1L;

    public RegistroNoEncontradoException() {
        super("El registro no fue encontrado.");
    }

    public RegistroNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RegistroNoEncontradoException(String entidad, Long id) {
        super("No se encontro " + entidad + " con ID: " + id);
    }
}