package rs.ac.singidunum.consultme.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import rs.ac.singidunum.consultme.dto.LoginRequest;
import rs.ac.singidunum.consultme.dto.LoginResponse;
import rs.ac.singidunum.consultme.service.AuthService;

// Rute za prijavu. "/api" na početku -> Angular proxy zna da to šalje backendu.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	// POST
	// ruta: /api/auth/login  (dostupna bez tokena - vidi SecurityConfig)
	// @Valid -> pokreće validacije iz LoginRequest (@NotBlank, @Email)
	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest zahtev) {
		return authService.prijava(zahtev);
	}

}
