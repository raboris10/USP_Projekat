package rs.ac.singidunum.consultme.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import rs.ac.singidunum.consultme.dto.PrijavljeniStudentDto;
import rs.ac.singidunum.consultme.dto.TerminDto;
import rs.ac.singidunum.consultme.dto.TerminIzmenaRequest;
import rs.ac.singidunum.consultme.dto.TerminRequest;
import rs.ac.singidunum.consultme.service.TerminService;

// Termini konsultacija.
// Profesor pravi, menja i otkazuje SVOJE termine; svi prijavljeni mogu da pretražuju aktivne termine.
// authentication.getName() -> email prijavljenog korisnika (iz tokena), tako backend zna ko je profesor.
@RestController
@RequestMapping("/api/termini")
public class TerminController {

	private final TerminService terminService;

	public TerminController(TerminService terminService) {
		this.terminService = terminService;
	}

	// GET
	// ruta: /api/termini?profesorId=4&predmetId=1&od=2026-12-01&do=2026-12-31  (svi prijavljeni)
	// Svi parametri su opcioni. Vraća samo aktivne termine koji tek dolaze.
	// "do" je rezervisana reč u Javi, pa se promenljiva zove doDatuma, a u adresi ostaje "do".
	@GetMapping
	public List<TerminDto> pretrazi(
			@RequestParam(required = false) Long profesorId,
			@RequestParam(required = false) Long predmetId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate od,
			@RequestParam(name = "do", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate doDatuma) {
		return terminService.pretrazi(profesorId, predmetId, od, doDatuma);
	}

	// GET
	// ruta: /api/termini/moji  (samo PROFESOR) -> svi termini prijavljenog profesora
	// Spring prvo proba tačnu putanju "/moji", pa tek onda "/{id}", tako da se ove dve rute ne mešaju.
	@GetMapping("/moji")
	@PreAuthorize("hasRole('PROFESOR')")
	public List<TerminDto> moji(Authentication authentication) {
		return terminService.moji(authentication.getName());
	}

	// GET
	// ruta: /api/termini/{id}  (svi prijavljeni)
	@GetMapping("/{id}")
	public TerminDto findById(@PathVariable Long id) {
		return terminService.findById(id);
	}

	// GET
	// ruta: /api/termini/{id}/prijavljeni  (samo PROFESOR, i to samo za svoj termin)
	@GetMapping("/{id}/prijavljeni")
	@PreAuthorize("hasRole('PROFESOR')")
	public List<PrijavljeniStudentDto> prijavljeni(@PathVariable Long id, Authentication authentication) {
		return terminService.prijavljeni(authentication.getName(), id);
	}

	// POST
	// ruta: /api/termini  (samo PROFESOR)
	// Vraća listu, jer ponavljajući termin pravi više termina odjednom.
	@PostMapping
	@PreAuthorize("hasRole('PROFESOR')")
	@ResponseStatus(HttpStatus.CREATED)
	public List<TerminDto> create(@Valid @RequestBody TerminRequest zahtev, Authentication authentication) {
		return terminService.create(authentication.getName(), zahtev);
	}

	// PUT
	// ruta: /api/termini/{id}  (samo PROFESOR, i to samo svoj termin) -> pomeranje / izmena
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('PROFESOR')")
	public TerminDto update(@PathVariable Long id, @Valid @RequestBody TerminIzmenaRequest zahtev,
			Authentication authentication) {
		return terminService.update(authentication.getName(), id, zahtev);
	}

	// PATCH
	// ruta: /api/termini/{id}/otkazi  (samo PROFESOR, i to samo svoj termin)
	// PATCH -> menja se samo deo podatka (status), a ne ceo termin
	@PatchMapping("/{id}/otkazi")
	@PreAuthorize("hasRole('PROFESOR')")
	public TerminDto otkazi(@PathVariable Long id, Authentication authentication) {
		return terminService.otkazi(authentication.getName(), id);
	}

}
