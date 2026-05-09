package co.edu.unbosque.artcook.exception;

public class NombreException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public NombreException() {
		super("El nombre es inválido");
	}
	
	public NombreException(String mensaje) {
		super(mensaje);
	}
}
