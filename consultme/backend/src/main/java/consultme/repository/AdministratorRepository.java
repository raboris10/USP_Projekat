package consultme.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import consultme.model.Administrator;

public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
}
