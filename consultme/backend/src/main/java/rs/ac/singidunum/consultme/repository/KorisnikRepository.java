package rs.ac.singidunum.consultme.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rs.ac.singidunum.consultme.model.Korisnik;
import rs.ac.singidunum.consultme.model.Uloga;

// JpaRepository<Korisnik, Long> nam automatski daje findAll, findById, save, delete...
public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {

	// Spring sam napravi SQL iz imena metode:
	// SELECT * FROM korisnik WHERE email = ?
	// Optional -> rezultat može i da ne postoji (pogrešan email)
	Optional<Korisnik> findByEmail(String email);

	// Aktivni korisnici sa datom ulogom, abecedno (npr. lista profesora za filter)
	// SELECT * FROM korisnik WHERE uloga = ? AND aktivan = true ORDER BY prezime, ime
	List<Korisnik> findByUlogaAndAktivanTrueOrderByPrezimeAscImeAsc(Uloga uloga);

	// Provera jedinstvenosti email-a (za dodavanje i izmenu korisnika)
	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

	// Pretraga za administratora. Oba filtera su opciona (null -> uslov se preskače).
	// pretraga stiže već pripremljena kao "%tekst%" malim slovima.
	@Query("""
			select k from Korisnik k
			where (:uloga is null or k.uloga = :uloga)
			  and (:pretraga is null
			       or lower(concat(k.ime, ' ', k.prezime)) like :pretraga
			       or lower(k.email) like :pretraga)
			order by k.prezime, k.ime
			""")
	List<Korisnik> pretrazi(@Param("uloga") Uloga uloga, @Param("pretraga") String pretraga);

}
