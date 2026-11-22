package rs.ac.singidunum.consultme.exception;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Hvata greške iz svih kontrolera i vraća ih frontendu u istom obliku:
// { "poruka": "..." }
// Tako frontend uvek zna gde da pročita tekst greške.
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 401 - pogrešan email ili lozinka
	@ExceptionHandler(NeispravnaPrijavaException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public Map<String, String> handleNeispravnaPrijava(NeispravnaPrijavaException ex) {
		return Map.of("poruka", ex.getMessage());
	}

	// 404 - podatak ne postoji
	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public Map<String, String> handleNotFound(ResourceNotFoundException ex) {
		return Map.of("poruka", ex.getMessage());
	}

	// 400 - zahtev krši pravilo aplikacije (npr. duplikat, brisanje podatka koji se koristi)
	@ExceptionHandler(PoslovnaGreskaException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, String> handlePoslovnaGreska(PoslovnaGreskaException ex) {
		return Map.of("poruka", ex.getMessage());
	}

	// 409 - baza je odbila upis (npr. duplikat koji je "proleteo" pored provere u servisu).
	// Ovo je zaštitna mreža; u normalnom radu servis uhvati grešku pre baze.
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public Map<String, String> handleDataIntegrity(DataIntegrityViolationException ex) {
		return Map.of("poruka", "Podatak nije sačuvan jer krši ograničenja baze (duplikat ili je u upotrebi).");
	}

	// 400 - nije prošla validacija (@Valid), npr. prazan email.
	// Vraćamo poruku prve greške (one iz anotacija @NotBlank, @Email...).
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
		String poruka = ex.getBindingResult().getFieldErrors().stream()
				.map(greska -> greska.getDefaultMessage())
				.findFirst()
				.orElse("Neispravni podaci.");
		return Map.of("poruka", poruka);
	}

}
