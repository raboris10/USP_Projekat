package rs.ac.singidunum.consultme.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.ac.singidunum.consultme.model.Katedra;

public interface KatedraRepository extends JpaRepository<Katedra, Long> {

	// Da li već postoji katedra sa ovim nazivom (bez obzira na velika/mala slova)
	boolean existsByNazivIgnoreCase(String naziv);

	// Isto, ali ne računajući katedru koju menjamo (za izmenu)
	boolean existsByNazivIgnoreCaseAndIdNot(String naziv, Long id);

}
