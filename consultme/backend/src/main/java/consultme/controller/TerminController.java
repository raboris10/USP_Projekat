package consultme.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import consultme.exception.ConsultMeException;
import consultme.model.Rezervacija;
import consultme.model.Termin;
import consultme.service.EvidencijaService;
import consultme.service.RezervacijaService;
import consultme.service.TerminService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/termini")
public class TerminController {

	@Autowired
	private TerminService terminService;
	@Autowired
	private RezervacijaService rezervacijaService;
	@Autowired
	private EvidencijaService evidencijaService;

	// pretraga termina: ?nastavnikId=&predmetId=&od=2026-12-01T00:00:00&do=2026-12-31T23:59:59
	@GetMapping
	public ResponseEntity<List<Termin>> pretraga(
			@RequestParam(required = false) Long nastavnikId,
			@RequestParam(required = false) Long predmetId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime od,
			@RequestParam(name = "do", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime doVremena) {
		return ResponseEntity.status(HttpStatus.OK).body(terminService.pretrazi(nastavnikId, predmetId, od, doVremena));
	}

	// termini prijavljenog nastavnika
	@GetMapping("/moji")
	public ResponseEntity<List<Termin>> getMoji(Authentication authentication) {
		return ResponseEntity.status(HttpStatus.OK).body(terminService.getMoji(authentication.getName()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Termin> getById(@PathVariable Long id) {
		return ResponseEntity.status(HttpStatus.OK).body(terminService.getById(id));
	}

	// nastavnik definise termin; predmeti se salju kao [{"id": 1}]
	@PostMapping
	public ResponseEntity<Termin> create(@Valid @RequestBody Termin termin, Authentication authentication) {
		return ResponseEntity.status(HttpStatus.CREATED).body(terminService.create(authentication.getName(), termin));
	}

	// pomeranje termina: {"pocetak": "...", "kraj": "...", "mesto": "..."}
	@PatchMapping("/{id}/pomeri")
	public ResponseEntity<Termin> pomeri(@PathVariable Long id, @RequestBody Map<String, String> body,
			Authentication authentication) {
		if (body.get("pocetak") == null || body.get("kraj") == null) {
			throw new ConsultMeException("Pocetak i kraj su obavezni");
		}
		LocalDateTime pocetak = LocalDateTime.parse(body.get("pocetak"));
		LocalDateTime kraj = LocalDateTime.parse(body.get("kraj"));
		return ResponseEntity.status(HttpStatus.OK)
				.body(terminService.pomeri(authentication.getName(), id, pocetak, kraj, body.get("mesto")));
	}

	@PatchMapping("/{id}/otkazi")
	public ResponseEntity<Termin> otkazi(@PathVariable Long id, Authentication authentication) {
		return ResponseEntity.status(HttpStatus.OK).body(terminService.otkazi(authentication.getName(), id));
	}

	// rezervacije jednog termina (samo nastavnik koji ga je objavio)
	@GetMapping("/{id}/rezervacije")
	public ResponseEntity<List<Rezervacija>> getRezervacije(@PathVariable Long id, Authentication authentication) {
		return ResponseEntity.status(HttpStatus.OK).body(rezervacijaService.getByTermin(authentication.getName(), id));
	}

	// izvoz evidencije dolazaka u CSV
	@GetMapping("/{id}/evidencija/csv")
	public ResponseEntity<byte[]> izvozCsv(@PathVariable Long id, Authentication authentication) {
		String csv = evidencijaService.izvozCsv(authentication.getName(), id);
		return ResponseEntity.status(HttpStatus.OK)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=evidencija-termin-" + id + ".csv")
				.contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
				.body(csv.getBytes(StandardCharsets.UTF_8));
	}
}
