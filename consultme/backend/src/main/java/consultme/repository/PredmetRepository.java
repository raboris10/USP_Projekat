package consultme.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import consultme.model.Predmet;

public interface PredmetRepository extends JpaRepository<Predmet, Long> {

	boolean existsBySifra(String sifra);

	boolean existsByKatedraId(Long katedraId);

	List<Predmet> findByKatedraId(Long katedraId);
}
