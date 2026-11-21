package consultme.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import consultme.exception.ConsultMeException;
import consultme.model.Rezervacija;
import consultme.service.EvidencijaService;
import consultme.service.RezervacijaService;

@RestController
@RequestMapping("/api/rezervacije")
public class RezervacijaController {

	@Autowired
	private RezervacijaService rezervacijaService;
	@Autowired
	private EvidencijaService evidencijaService;

	// rezervacije prijavljenog studenta
	@GetMapping("/moje")
	public ResponseEntity<List<Rezervacija>> getMoje(Authentication authentication) {
		return ResponseEntity.status(HttpStatus.OK).body(rezervacijaService.getMoje(authentication.getName()));
	}

	// student rezervise termin: {"terminId": 1, "tema": "..."}
	@PostMapping
	public ResponseEntity<Rezervacija> rezervisi(@RequestBody Map<String, Object> body, Authentication authentication) {
		if (body.get("terminId") == null) {
			throw new ConsultMeException("terminId je obavezan");
		}
		Long terminId = Long.valueOf(body.get("terminId").toString());
		String tema = body.get("tema") != null ? body.get("tema").toString() : null;
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(rezervacijaService.rezervisi(authentication.getName(), terminId, tema));
	}

	@PatchMapping("/{id}/otkazi")
	public ResponseEntity<Rezervacija> otkazi(@PathVariable Long id, Authentication authentication) {
		return ResponseEntity.status(HttpStatus.OK).body(rezervacijaService.otkazi(authentication.getName(), id));
	}

	// nastavnik belezi dolazak: {"dosao": true}
	@PatchMapping("/{id}/evidencija")
	public ResponseEntity<Rezervacija> evidentiraj(@PathVariable Long id, @RequestBody Map<String, Boolean> body,
			Authentication authentication) {
		if (body.get("dosao") == null) {
			throw new ConsultMeException("Polje dosao je obavezno");
		}
		return ResponseEntity.status(HttpStatus.OK)
				.body(evidencijaService.oznaci(authentication.getName(), id, body.get("dosao")));
	}
}
