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

import consultme.model.Katedra;
import consultme.service.KatedraService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/katedre")
public class KatedraController {

	@Autowired
	private KatedraService katedraService;

	@GetMapping
	public ResponseEntity<List<Katedra>> getAll() {
		return ResponseEntity.status(HttpStatus.OK).body(katedraService.getAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Katedra> getById(@PathVariable Long id) {
		return ResponseEntity.status(HttpStatus.OK).body(katedraService.getById(id));
	}

	@PostMapping
	public ResponseEntity<Katedra> create(@Valid @RequestBody Katedra katedra) {
		return ResponseEntity.status(HttpStatus.CREATED).body(katedraService.create(katedra));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Katedra> update(@PathVariable Long id, @Valid @RequestBody Katedra katedra) {
		return ResponseEntity.status(HttpStatus.OK).body(katedraService.update(id, katedra));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable Long id) {
		katedraService.delete(id);
		return ResponseEntity.status(HttpStatus.OK).body("Katedra uspesno obrisana");
	}
}
