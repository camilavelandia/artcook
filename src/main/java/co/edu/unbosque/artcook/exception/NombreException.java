package co.edu.unbosque.artcook.exception;

/**
 * Excepción cuando el prompt para generar la receta está vacío o es muy corto.
 */
public class PromptVacioException extends Exception {
    private static final long serialVersionUID = 1L;

    public PromptVacioException() {
        super("El prompt no puede estar vacio. Describe la receta o manualidad que deseas generar.");
    }

    public PromptVacioException(String mensaje) {
        super(mensaje);
    }
}
