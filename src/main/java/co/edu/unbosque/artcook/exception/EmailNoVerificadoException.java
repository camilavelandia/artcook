package co.edu.unbosque.artcook.exception;

public class EmailNoVerificadoException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public EmailNoVerificadoException() {
		super("El email no ha sido verificado");
	}
	
	public EmailNoVerificadoException(String mensaje) {
		super(mensaje);
	}
}