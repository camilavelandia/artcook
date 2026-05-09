package co.edu.unbosque.artcook.exception;

public class TokenInvalidoException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public TokenInvalidoException() {
		super("El token de verificación es inválido o ha expirado");
	}
	
	public TokenInvalidoException(String mensaje) {
		super(mensaje);
	}
}
