package consultme.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import consultme.model.Korisnik;
import consultme.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private AuthService authService;

	// prijava: {"email": "...", "lozinka": "..."} -> vraca JWT token
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
		return ResponseEntity.status(HttpStatus.OK).body(authService.login(body.get("email"), body.get("lozinka")));
	}

	// podaci o trenutno prijavljenom korisniku (na osnovu tokena)
	@GetMapping("/me")
	public ResponseEntity<Korisnik> me(Authentication authentication) {
		return ResponseEntity.status(HttpStatus.OK).body(authService.getPrijavljeni(authentication.getName()));
	}
}
