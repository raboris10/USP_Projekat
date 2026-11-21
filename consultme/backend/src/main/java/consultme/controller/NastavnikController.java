package consultme.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import consultme.model.Nastavnik;
import consultme.service.NastavnikService;
import jakarta.validation.Valid;

// GET: svaki prijavljeni korisnik, ostalo: samo administrator (pravila su u SecurityConfig)
@RestController
@RequestMapping("/api/nastavnici")
public class NastavnikController {

	@Autowired
	private NastavnikService nastavnikService;

	@GetMapping
	public ResponseEntity<List<Nastavnik>> getAll() {
		return ResponseEntity.status(HttpStatus.OK).body(nastavnikService.getAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Nastavnik> getById(@PathVariable Long id) {
		return ResponseEntity.status(HttpStatus.OK).body(nastavnikService.getById(id));
	}

	@GetMapping("/katedra/{katedraId}")
	public ResponseEntity<List<Nastavnik>> getByKatedra(@PathVariable Long katedraId) {
		return ResponseEntity.status(HttpStatus.OK).body(nastavnikService.getByKatedra(katedraId));
	}

	// pretraga nastavnika po imenu/prezimenu
	@GetMapping("/pretraga")
	public ResponseEntity<List<Nastavnik>> pretrazi(@RequestParam String ime) {
		return ResponseEntity.status(HttpStatus.OK).body(nastavnikService.pretraziPoImenu(ime));
	}

	@PostMapping("/katedra/{katedraId}")
	public ResponseEntity<Nastavnik> create(@PathVariable Long katedraId, @Valid @RequestBody Nastavnik nastavnik) {
		return ResponseEntity.status(HttpStatus.CREATED).body(nastavnikService.create(katedraId, nastavnik));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Nastavnik> update(@PathVariable Long id, @Valid @RequestBody Nastavnik nastavnik) {
		return ResponseEntity.status(HttpStatus.OK).body(nastavnikService.update(id, nastavnik));
	}

	// prebacivanje nastavnika na drugu katedru
	@PatchMapping("/{id}/katedra/{katedraId}")
	public ResponseEntity<Nastavnik> promeniKatedru(@PathVariable Long id, @PathVariable Long katedraId) {
		return ResponseEntity.status(HttpStatus.OK).body(nastavnikService.promeniKatedru(id, katedraId));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable Long id) {
		nastavnikService.delete(id);
		return ResponseEntity.status(HttpStatus.OK).body("Nastavnik uspesno obrisan");
	}
}
