// clase modificada camila-juan

package co.edu.unbosque.artcook.exception;

public class ContrasenaException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public ContrasenaException() {
		super("La contraseña no cumple con los requisitos");
	}
	
	public ContrasenaException(String mensaje) {
		super(mensaje);
	}
}
