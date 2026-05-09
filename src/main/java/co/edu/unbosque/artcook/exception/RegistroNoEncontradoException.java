package co.edu.unbosque.artcook.exception;

public class RegistroNoEncontradoException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public RegistroNoEncontradoException() {
		super("El registro no fue encontrado");
	}
	
	public RegistroNoEncontradoException(String mensaje) {
		super(mensaje);
	}
}
