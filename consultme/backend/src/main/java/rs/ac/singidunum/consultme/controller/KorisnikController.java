package rs.ac.singidunum.consultme.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rs.ac.singidunum.consultme.dto.KorisnikDto;
import rs.ac.singidunum.consultme.service.KorisnikService;

@RestController
@RequestMapping("/api/korisnici")
public class KorisnikController {

	private final KorisnikService korisnikService;

	public KorisnikController(KorisnikService korisnikService) {
		this.korisnikService = korisnikService;
	}

	// GET
	// ruta: /api/korisnici/me  (ZAŠTIĆENA - radi samo sa ispravnim tokenom)
	// Vraća podatke o trenutno prijavljenom korisniku.
	// Spring sam prosleđuje "authentication" - to je objekat koji je JwtFilter napravio iz tokena,
	// a getName() vraća email (subject iz tokena).
	@GetMapping("/me")
	public KorisnikDto me(Authentication authentication) {
		return korisnikService.findByEmail(authentication.getName());
	}

	// GET
	// ruta: /api/korisnici/profesori  (svi prijavljeni)
	// Lista profesora - student je koristi kao filter pri pretrazi termina.
	@GetMapping("/profesori")
	public List<KorisnikDto> profesori() {
		return korisnikService.findProfesori();
	}

}
