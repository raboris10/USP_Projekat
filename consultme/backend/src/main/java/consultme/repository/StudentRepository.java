package consultme.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import consultme.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

	Optional<Student> findByEmail(String email);

	boolean existsByBrojIndeksa(String brojIndeksa);
}
