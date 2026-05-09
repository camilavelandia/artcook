package co.edu.unbosque.artcook.exception;

public class CredencialesInvalidasException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public CredencialesInvalidasException() {
		super("Email o contraseña incorrectos");
	}
	
	public CredencialesInvalidasException(String mensaje) {
		super(mensaje);
	}
}
