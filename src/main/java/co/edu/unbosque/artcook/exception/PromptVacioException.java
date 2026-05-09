package co.edu.unbosque.artcook.exception;

public class PromptVacioException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public PromptVacioException() {
		super("El prompt no puede estar vacío o es muy corto");
	}
	
	public PromptVacioException(String mensaje) {
		super(mensaje);
	}
}
