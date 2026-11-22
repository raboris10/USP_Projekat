package rs.ac.singidunum.consultme.exception;

import java.util.Map;

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
