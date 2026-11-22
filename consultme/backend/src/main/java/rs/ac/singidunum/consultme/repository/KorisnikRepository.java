package rs.ac.singidunum.consultme.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

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

}
