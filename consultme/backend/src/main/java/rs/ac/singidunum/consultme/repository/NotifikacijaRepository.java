package rs.ac.singidunum.consultme.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.ac.singidunum.consultme.model.Notifikacija;
import rs.ac.singidunum.consultme.model.TipNotifikacije;

public interface NotifikacijaRepository extends JpaRepository<Notifikacija, Long> {

	// Sve notifikacije jednog korisnika, najnovije prve
	// SELECT * FROM notifikacija WHERE primalac_id = ? ORDER BY kreirana DESC
	List<Notifikacija> findByPrimalacIdOrderByKreiranaDesc(Long primalacId);

	// Broj nepročitanih (broj na "zvoncu")
	// SELECT COUNT(*) FROM notifikacija WHERE primalac_id = ? AND procitana = false
	long countByPrimalacIdAndProcitanaFalse(Long primalacId);

	// Da li je korisnik već dobio ovu vrstu notifikacije za ovaj termin
	// (da se isti podsetnik ne pošalje dva puta)
	boolean existsByPrimalacIdAndTerminIdAndTip(Long primalacId, Long terminId, TipNotifikacije tip);

}
