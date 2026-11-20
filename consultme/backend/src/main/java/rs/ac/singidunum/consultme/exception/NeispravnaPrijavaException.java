package rs.ac.singidunum.consultme.exception;

// Bacamo je kad su email ili lozinka pogrešni (vraća se 401)
public class NeispravnaPrijavaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NeispravnaPrijavaException(String message) {
		super(message);
	}

}
