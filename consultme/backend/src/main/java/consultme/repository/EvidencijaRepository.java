package consultme.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import consultme.model.Evidencija;

public interface EvidencijaRepository extends JpaRepository<Evidencija, Long> {
}
