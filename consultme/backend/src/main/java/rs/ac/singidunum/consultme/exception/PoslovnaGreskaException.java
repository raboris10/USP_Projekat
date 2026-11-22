package rs.ac.singidunum.consultme.exception;

// Bacamo je kad zahtev krši neko pravilo aplikacije (vraća se 400), npr:
// - katedra sa tim nazivom već postoji
// - predmet ne može da se obriše jer za njega postoje termini
public class PoslovnaGreskaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PoslovnaGreskaException(String message) {
		super(message);
	}

}
