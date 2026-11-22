package rs.ac.singidunum.consultme.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rs.ac.singidunum.consultme.dto.NotifikacijaDto;
import rs.ac.singidunum.consultme.service.NotifikacijaService;

// Notifikacije prijavljenog korisnika - dostupno svim ulogama.
// Svako vidi i menja samo SVOJE notifikacije (korisnik se prepoznaje iz tokena).
@RestController
@RequestMapping("/api/notifikacije")
public class NotifikacijaController {

	private final NotifikacijaService notifikacijaService;

	public NotifikacijaController(NotifikacijaService notifikacijaService) {
		this.notifikacijaService = notifikacijaService;
	}

	// GET
	// ruta: /api/notifikacije/moje -> sve notifikacije, najnovije prve
	@GetMapping("/moje")
	public List<NotifikacijaDto> moje(Authentication authentication) {
		return notifikacijaService.moje(authentication.getName());
	}

	// GET
	// ruta: /api/notifikacije/broj-neprocitanih -> { "broj": 3 }
	// Mali odgovor za "zvonce" u zaglavlju - frontend ga može često proveravati.
	@GetMapping("/broj-neprocitanih")
	public Map<String, Long> brojNeprocitanih(Authentication authentication) {
		return Map.of("broj", notifikacijaService.brojNeprocitanih(authentication.getName()));
	}

	// PATCH
	// ruta: /api/notifikacije/{id}/procitana -> označi jednu kao pročitanu
	@PatchMapping("/{id}/procitana")
	public NotifikacijaDto oznaciKaoProcitanu(@PathVariable Long id, Authentication authentication) {
		return notifikacijaService.oznaciKaoProcitanu(authentication.getName(), id);
	}

	// PATCH
	// ruta: /api/notifikacije/procitane-sve -> označi sve kao pročitane; vraća { "oznaceno": 5 }
	@PatchMapping("/procitane-sve")
	public Map<String, Integer> oznaciSveKaoProcitane(Authentication authentication) {
		return Map.of("oznaceno", notifikacijaService.oznaciSveKaoProcitane(authentication.getName()));
	}

}
