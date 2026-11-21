package consultme.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
		return greska(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(ConsultMeException.class)
	public ResponseEntity<Map<String, Object>> handleConsultMe(ConsultMeException ex) {
		return greska(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
		return greska(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
		return greska(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		String poruka = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + ": " + e.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return greska(HttpStatus.BAD_REQUEST, poruka);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex) {
		return greska(HttpStatus.CONFLICT, "Operacija krsi ogranicenja baze (duplikat ili je podatak u upotrebi)");
	}

	private ResponseEntity<Map<String, Object>> greska(HttpStatus status, String poruka) {
		Map<String, Object> telo = new LinkedHashMap<>();
		telo.put("status", status.value());
		telo.put("poruka", poruka);
		telo.put("vreme", LocalDateTime.now().toString());
		return ResponseEntity.status(status).body(telo);
	}
}
