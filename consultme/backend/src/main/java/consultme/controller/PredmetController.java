package consultme.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import consultme.model.Predmet;
import consultme.service.PredmetService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/predmeti")
public class PredmetController {

	@Autowired
	private PredmetService predmetService;

	@GetMapping
	public ResponseEntity<List<Predmet>> getAll() {
		return ResponseEntity.status(HttpStatus.OK).body(predmetService.getAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Predmet> getById(@PathVariable Long id) {
		return ResponseEntity.status(HttpStatus.OK).body(predmetService.getById(id));
	}

	@GetMapping("/katedra/{katedraId}")
	public ResponseEntity<List<Predmet>> getByKatedra(@PathVariable Long katedraId) {
		return ResponseEntity.status(HttpStatus.OK).body(predmetService.getByKatedra(katedraId));
	}

	@PostMapping("/katedra/{katedraId}")
	public ResponseEntity<Predmet> create(@PathVariable Long katedraId, @Valid @RequestBody Predmet predmet) {
		return ResponseEntity.status(HttpStatus.CREATED).body(predmetService.create(katedraId, predmet));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Predmet> update(@PathVariable Long id, @Valid @RequestBody Predmet predmet) {
		return ResponseEntity.status(HttpStatus.OK).body(predmetService.update(id, predmet));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable Long id) {
		predmetService.delete(id);
		return ResponseEntity.status(HttpStatus.OK).body("Predmet uspesno obrisan");
	}
}
