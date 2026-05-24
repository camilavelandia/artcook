package co.edu.unbosque.artcook.exception;

/**
 * Excepción que se lanza cuando el prompt para generar la receta está vacío o es muy corto.
 */
public class PromptVacioException extends Exception {

    /** Identificador de versión para la serialización de la clase. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje genérico de prompt vacío.
     */
    public PromptVacioException() {
        super("El prompt no puede estar vacio. Describe la receta o manualidad que deseas generar.");
    }

    /**
     * Crea la excepción con un mensaje personalizado.
     *
     * @param mensaje descripción específica del error de prompt
     */
    public PromptVacioException(String mensaje) {
        super(mensaje);
    }
}