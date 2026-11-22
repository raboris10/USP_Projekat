package rs.ac.singidunum.consultme.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.ac.singidunum.consultme.model.Termin;

public interface TerminRepository extends JpaRepository<Termin, Long> {

	// Da li za predmet postoji bar jedan termin (tada predmet ne sme da se obriše)
	boolean existsByPredmetId(Long predmetId);

}
