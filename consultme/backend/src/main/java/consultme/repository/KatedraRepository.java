package consultme.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import consultme.model.Katedra;

public interface KatedraRepository extends JpaRepository<Katedra, Long> {
}
