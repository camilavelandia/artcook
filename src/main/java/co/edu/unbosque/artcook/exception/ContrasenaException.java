package co.edu.unbosque.artcook.exception;

/**
 * Excepción cuando la contraseña no cumple los requisitos mínimos.
 */
public class ContrasenaException extends Exception {
    private static final long serialVersionUID = 1L;

    public ContrasenaException() {
        super("La contrasena debe tener minimo 8 caracteres, al menos un numero y un caracter especial.");
    }

    public ContrasenaException(String mensaje) {
        super(mensaje);
    }
}
