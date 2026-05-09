package co.edu.unbosque.artcook.exception;

public class EmailDuplicadoException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public EmailDuplicadoException() {
		super("El email ya está registrado");
	}
	
	public EmailDuplicadoException(String mensaje) {
		super(mensaje);
	}
}