package rs.ac.singidunum.consultme.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.ac.singidunum.consultme.model.Predmet;

public interface PredmetRepository extends JpaRepository<Predmet, Long> {

	// Predmeti jedne katedre, abecedno
	// SELECT * FROM predmet WHERE katedra_id = ? ORDER BY naziv
	List<Predmet> findByKatedraIdOrderByNazivAsc(Long katedraId);

	// Da li katedra ima bar jedan predmet (tada ne sme da se obriše)
	boolean existsByKatedraId(Long katedraId);

	// Provera jedinstvenosti šifre (bez obzira na velika/mala slova)
	boolean existsBySifraIgnoreCase(String sifra);

	boolean existsBySifraIgnoreCaseAndIdNot(String sifra, Long id);

	// Da li je profesor dodeljen bar jednom predmetu (za proveru pre promene uloge)
	// (ProfesoriId -> traži po id-ju unutar kolekcije profesori)
	boolean existsByProfesoriId(Long profesorId);

}
