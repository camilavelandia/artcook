package co.edu.unbosque.artcook.exception;

public class EmailFormatoException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public EmailFormatoException() {
		super("El formato del email es inválido");
	}
	
	public EmailFormatoException(String mensaje) {
		super(mensaje);
	}
}
