package rs.ac.singidunum.consultme.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import rs.ac.singidunum.consultme.dto.RezervacijaDto;
import rs.ac.singidunum.consultme.dto.RezervacijaRequest;
import rs.ac.singidunum.consultme.service.RezervacijaService;

// Rezervacije - sve rute su samo za studente.
// (Profesor svoje prijavljene vidi preko /api/termini/{id}/prijavljeni.)
@RestController
@RequestMapping("/api/rezervacije")
@PreAuthorize("hasRole('STUDENT')")   // važi za SVE metode u ovoj klasi
public class RezervacijaController {

	private final RezervacijaService rezervacijaService;

	public RezervacijaController(RezervacijaService rezervacijaService) {
		this.rezervacijaService = rezervacijaService;
	}

	// GET
	// ruta: /api/rezervacije/moje -> sve rezervacije prijavljenog studenta
	@GetMapping("/moje")
	public List<RezervacijaDto> moje(Authentication authentication) {
		return rezervacijaService.moje(authentication.getName());
	}

	// POST
	// ruta: /api/rezervacije -> rezervacija termina
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RezervacijaDto rezervisi(@Valid @RequestBody RezervacijaRequest zahtev, Authentication authentication) {
		return rezervacijaService.rezervisi(authentication.getName(), zahtev);
	}

	// PATCH
	// ruta: /api/rezervacije/{id}/otkazi -> otkazivanje (najkasnije 2 sata pre termina)
	@PatchMapping("/{id}/otkazi")
	public RezervacijaDto otkazi(@PathVariable Long id, Authentication authentication) {
		return rezervacijaService.otkazi(authentication.getName(), id);
	}

}
