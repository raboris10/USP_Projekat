package consultme.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import consultme.model.Korisnik;

public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {

	Optional<Korisnik> findByEmail(String email);

	boolean existsByEmail(String email);
}
