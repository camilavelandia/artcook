package co.edu.unbosque.artcook.exception;

public class UsuarioInactivoException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public UsuarioInactivoException() {
		super("El usuario ha sido desactivado por el administrador");
	}
	
	public UsuarioInactivoException(String mensaje) {
		super(mensaje);
	}
}
