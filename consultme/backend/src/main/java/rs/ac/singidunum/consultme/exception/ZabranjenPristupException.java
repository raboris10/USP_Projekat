package rs.ac.singidunum.consultme.exception;

// Bacamo je kad korisnik ima pravu ulogu, ali dira tuđi podatak (vraća se 403), npr:
// profesor pokušava da pomeri termin drugog profesora.
// (Pogrešnu ulogu hvata @PreAuthorize, a ovo je provera "da li je baš tvoje".)
public class ZabranjenPristupException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ZabranjenPristupException(String message) {
		super(message);
	}

}
