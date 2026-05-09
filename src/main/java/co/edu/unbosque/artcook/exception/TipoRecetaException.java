package co.edu.unbosque.artcook.exception;

public class TipoRecetaException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public TipoRecetaException() {
		super("El tipo de receta es inválido o nulo");
	}
	
	public TipoRecetaException(String mensaje) {
		super(mensaje);
	}
}
