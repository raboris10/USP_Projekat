package consultme.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import consultme.model.Nastavnik;

public interface NastavnikRepository extends JpaRepository<Nastavnik, Long> {

	Optional<Nastavnik> findByEmail(String email);

	List<Nastavnik> findByKatedraId(Long katedraId);

	boolean existsByKatedraId(Long katedraId);

	@Query("select n from Nastavnik n where lower(n.ime) like lower(concat('%', :ime, '%')) "
			+ "or lower(n.prezime) like lower(concat('%', :ime, '%'))")
	List<Nastavnik> pretraziPoImenu(@Param("ime") String ime);
}
