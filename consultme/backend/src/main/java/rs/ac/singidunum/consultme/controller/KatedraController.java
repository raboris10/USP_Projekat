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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import rs.ac.singidunum.consultme.dto.KatedraDto;
import rs.ac.singidunum.consultme.dto.KatedraRequest;
import rs.ac.singidunum.consultme.service.KatedraService;

// Katedre: pregled je dostupan svim prijavljenim korisnicima, a izmene samo administratoru.
@RestController
@RequestMapping("/api/katedre")
public class KatedraController {

	private final KatedraService katedraService;

	public KatedraController(KatedraService katedraService) {
		this.katedraService = katedraService;
	}

	// GET
	// ruta: /api/katedre  (svi prijavljeni)
	@GetMapping
	public List<KatedraDto> findAll() {
		return katedraService.findAll();
	}

	// GET
	// ruta: /api/katedre/{id}  (svi prijavljeni)
	@GetMapping("/{id}")
	public KatedraDto findById(@PathVariable Long id) {
		return katedraService.findById(id);
	}

	// POST
	// ruta: /api/katedre  (samo ADMIN)
	// 201 Created -> standardni odgovor kad je nešto novo napravljeno
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public KatedraDto create(@Valid @RequestBody KatedraRequest zahtev) {
		return katedraService.create(zahtev);
	}

	// PUT
	// ruta: /api/katedre/{id}  (samo ADMIN)
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public KatedraDto update(@PathVariable Long id, @Valid @RequestBody KatedraRequest zahtev) {
		return katedraService.update(id, zahtev);
	}

	// DELETE
	// ruta: /api/katedre/{id}  (samo ADMIN)
	// 204 No Content -> obrisano, nema šta da se vrati
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		katedraService.delete(id);
	}

}
