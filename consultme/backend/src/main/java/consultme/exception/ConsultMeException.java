package consultme.exception;

public class ConsultMeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConsultMeException(String poruka) {
		super(poruka);
	}
}
