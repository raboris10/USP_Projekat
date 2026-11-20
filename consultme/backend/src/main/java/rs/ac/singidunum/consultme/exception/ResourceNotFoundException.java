package rs.ac.singidunum.consultme.exception;

// Bacamo je kad traženi podatak ne postoji u bazi (vraća se 404)
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
		super(message);
	}

}
