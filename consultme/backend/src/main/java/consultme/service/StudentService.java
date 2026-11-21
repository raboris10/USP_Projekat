package consultme.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import consultme.exception.ConsultMeException;
import consultme.model.Student;
import consultme.repository.KorisnikRepository;
import consultme.repository.RezervacijaRepository;
import consultme.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class StudentService {

	@Autowired
	private StudentRepository studentRepository;
	@Autowired
	private KorisnikRepository korisnikRepository;
	@Autowired
	private RezervacijaRepository rezervacijaRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	public List<Student> getAll() {
		return studentRepository.findAll();
	}

	public Student getById(Long id) {
		return studentRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Student sa ID " + id + " nije pronadjen"));
	}

	// admin dodaje studenta i odmah mu zadaje pocetnu lozinku
	@Transactional
	public Student create(Student student) {
		proveriEmail(student.getEmail(), null);
		if (studentRepository.existsByBrojIndeksa(student.getBrojIndeksa())) {
			throw new ConsultMeException("Student sa brojem indeksa " + student.getBrojIndeksa() + " vec postoji");
		}
		if (student.getLozinka() == null || student.getLozinka().isBlank()) {
			throw new ConsultMeException("Lozinka je obavezna");
		}
		student.setId(null);
		student.setLozinka(passwordEncoder.encode(student.getLozinka()));
		return studentRepository.save(student);
	}

	@Transactional
	public Student update(Long id, Student podaci) {
		Student student = getById(id);
		proveriEmail(podaci.getEmail(), id);
		if (!student.getBrojIndeksa().equals(podaci.getBrojIndeksa())
				&& studentRepository.existsByBrojIndeksa(podaci.getBrojIndeksa())) {
			throw new ConsultMeException("Student sa brojem indeksa " + podaci.getBrojIndeksa() + " vec postoji");
		}
		student.setIme(podaci.getIme());
		student.setPrezime(podaci.getPrezime());
		student.setEmail(podaci.getEmail());
		student.setBrojIndeksa(podaci.getBrojIndeksa());
		// lozinka se menja samo ako je poslata nova
		if (podaci.getLozinka() != null && !podaci.getLozinka().isBlank()) {
			student.setLozinka(passwordEncoder.encode(podaci.getLozinka()));
		}
		return studentRepository.save(student);
	}

	@Transactional
	public void delete(Long id) {
		Student student = getById(id);
		if (rezervacijaRepository.existsByStudentId(id)) {
			throw new ConsultMeException("Student se ne moze obrisati jer ima rezervacije");
		}
		studentRepository.delete(student);
	}

	// email mora biti jedinstven medju svim korisnicima
	private void proveriEmail(String email, Long idTrenutnog) {
		korisnikRepository.findByEmail(email).ifPresent(k -> {
			if (idTrenutnog == null || !k.getId().equals(idTrenutnog)) {
				throw new ConsultMeException("Korisnik sa emailom " + email + " vec postoji");
			}
		});
	}
}
