package consultme.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import consultme.model.Notifikacija;

public interface NotifikacijaRepository extends JpaRepository<Notifikacija, Long> {

	List<Notifikacija> findByRezervacijaStudentEmailOrderByVremeSlanjaDesc(String email);
}
