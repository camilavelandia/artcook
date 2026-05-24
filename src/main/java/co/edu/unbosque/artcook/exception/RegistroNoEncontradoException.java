package co.edu.unbosque.artcook.exception;

/**
 * Excepción que se lanza cuando un registro no se encuentra en la base de datos.
 */
public class RegistroNoEncontradoException extends Exception {

    /** Identificador de versión para la serialización de la clase. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje genérico de registro no encontrado.
     */
    public RegistroNoEncontradoException() {
        super("El registro no fue encontrado.");
    }

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param mensaje descripción específica del error de búsqueda
     */
    public RegistroNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    /**
     * Crea la excepción indicando la entidad y el ID que no fueron encontrados.
     *
     * @param entidad nombre de la entidad que se buscaba
     * @param id      identificador que no fue encontrado
     */
    public RegistroNoEncontradoException(String entidad, Long id) {
        super("No se encontro " + entidad + " con ID: " + id);
    }
}