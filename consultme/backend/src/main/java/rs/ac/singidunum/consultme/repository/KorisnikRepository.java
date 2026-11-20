package rs.ac.singidunum.consultme.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.ac.singidunum.consultme.model.Korisnik;

// JpaRepository<Korisnik, Long> nam automatski daje findAll, findById, save, delete...
public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {

	// Spring sam napravi SQL iz imena metode:
	// SELECT * FROM korisnik WHERE email = ?
	// Optional -> rezultat može i da ne postoji (pogrešan email)
	Optional<Korisnik> findByEmail(String email);

}
