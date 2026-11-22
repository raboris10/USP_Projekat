package rs.ac.singidunum.consultme.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import rs.ac.singidunum.consultme.dto.PredmetDto;
import rs.ac.singidunum.consultme.dto.PredmetRequest;
import rs.ac.singidunum.consultme.service.PredmetService;

// Predmeti: pregled je dostupan svim prijavljenim korisnicima, a izmene samo administratoru.
// Administrator pri dodavanju/izmeni predmeta bira i profesore koji ga drže.
@RestController
@RequestMapping("/api/predmeti")
public class PredmetController {

	private final PredmetService predmetService;

	public PredmetController(PredmetService predmetService) {
		this.predmetService = predmetService;
	}

	// GET
	// ruta: /api/predmeti               -> svi predmeti
	// ruta: /api/predmeti?katedraId=1   -> samo predmeti katedre 1
	// (svi prijavljeni)
	@GetMapping
	public List<PredmetDto> findAll(@RequestParam(required = false) Long katedraId) {
		return predmetService.findAll(katedraId);
	}

	// GET
	// ruta: /api/predmeti/{id}  (svi prijavljeni)
	@GetMapping("/{id}")
	public PredmetDto findById(@PathVariable Long id) {
		return predmetService.findById(id);
	}

	// POST
	// ruta: /api/predmeti  (samo ADMIN)
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public PredmetDto create(@Valid @RequestBody PredmetRequest zahtev) {
		return predmetService.create(zahtev);
	}

	// PUT
	// ruta: /api/predmeti/{id}  (samo ADMIN)
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public PredmetDto update(@PathVariable Long id, @Valid @RequestBody PredmetRequest zahtev) {
		return predmetService.update(id, zahtev);
	}

	// DELETE
	// ruta: /api/predmeti/{id}  (samo ADMIN)
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		predmetService.delete(id);
	}

}
