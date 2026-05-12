// clase modificada camila-juan

package co.edu.unbosque.artcook.exception;

public class CampoVacioException extends Exception {

	private static final long serialVersionUID = 1L;

	public CampoVacioException() {
		super("El campo no puede estar vacío");
	}
	
	public CampoVacioException(String mensaje) {
		super(mensaje);
	}
}
