package rs.ac.singidunum.consultme.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rs.ac.singidunum.consultme.model.Notifikacija;
import rs.ac.singidunum.consultme.model.TipNotifikacije;

public interface NotifikacijaRepository extends JpaRepository<Notifikacija, Long> {

	// Sve notifikacije jednog korisnika, najnovije prve
	// SELECT * FROM notifikacija WHERE primalac_id = ? ORDER BY kreirana DESC
	List<Notifikacija> findByPrimalacIdOrderByKreiranaDesc(Long primalacId);

	// Broj nepročitanih (broj na "zvoncu")
	// SELECT COUNT(*) FROM notifikacija WHERE primalac_id = ? AND procitana = false
	long countByPrimalacIdAndProcitanaFalse(Long primalacId);

	// Da li je korisnik već dobio ovu vrstu notifikacije za ovaj termin POSLE datog trenutka.
	// Koristi se za podsetnike: podsetnik "24h" se računa kao poslat samo ako je poslat
	// u trenutnom prozoru (posle "početak - 24h"). Ako je termin pomeren, stari podsetnik
	// je stariji od novog prozora, pa se šalje nov.
	boolean existsByPrimalacIdAndTerminIdAndTipAndKreiranaGreaterThanEqual(
			Long primalacId, Long terminId, TipNotifikacije tip, LocalDateTime posle);

	// Označi sve nepročitane notifikacije korisnika kao pročitane - JEDNIM upitom u bazi.
	// @Modifying -> upit menja podatke (UPDATE), ne čita ih; vraća broj izmenjenih redova.
	@Modifying
	@Query("""
			update Notifikacija n set n.procitana = true
			where n.primalac.id = :primalacId and n.procitana = false
			""")
	int oznaciSveKaoProcitane(@Param("primalacId") Long primalacId);

}
