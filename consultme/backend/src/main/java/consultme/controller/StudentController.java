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

import consultme.model.Student;
import consultme.service.StudentService;
import jakarta.validation.Valid;

// samo administrator (pravila su u SecurityConfig)
@RestController
@RequestMapping("/api/studenti")
public class StudentController {

	@Autowired
	private StudentService studentService;

	@GetMapping
	public ResponseEntity<List<Student>> getAll() {
		return ResponseEntity.status(HttpStatus.OK).body(studentService.getAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Student> getById(@PathVariable Long id) {
		return ResponseEntity.status(HttpStatus.OK).body(studentService.getById(id));
	}

	@PostMapping
	public ResponseEntity<Student> create(@Valid @RequestBody Student student) {
		return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(student));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Student> update(@PathVariable Long id, @Valid @RequestBody Student student) {
		return ResponseEntity.status(HttpStatus.OK).body(studentService.update(id, student));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable Long id) {
		studentService.delete(id);
		return ResponseEntity.status(HttpStatus.OK).body("Student uspesno obrisan");
	}
}
