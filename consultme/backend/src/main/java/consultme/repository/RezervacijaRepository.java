package consultme.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import consultme.model.Rezervacija;
import consultme.model.StatusRezervacije;

public interface RezervacijaRepository extends JpaRepository<Rezervacija, Long> {

	List<Rezervacija> findByStudentIdOrderByTerminPocetakDesc(Long studentId);

	List<Rezervacija> findByTerminIdOrderByKreiranaAsc(Long terminId);

	boolean existsByStudentId(Long studentId);

	boolean existsByStudentIdAndTerminIdAndStatus(Long studentId, Long terminId, StatusRezervacije status);

	long countByStudentIdAndStatusAndTerminPocetakAfter(Long studentId, StatusRezervacije status, LocalDateTime vreme);

	// aktivne rezervacije ciji termin pocinje uskoro, a podsetnik im jos nije poslat
	@Query("select r from Rezervacija r "
			+ "where r.status = consultme.model.StatusRezervacije.AKTIVNA "
			+ "and r.termin.status <> consultme.model.StatusTermina.OTKAZAN "
			+ "and r.termin.pocetak between :odVreme and :doVreme "
			+ "and not exists (select n from Notifikacija n where n.rezervacija = r "
			+ "and n.tip = consultme.model.TipNotifikacije.PODSETNIK)")
	List<Rezervacija> nadjiZaPodsetnik(@Param("odVreme") LocalDateTime odVreme,
									   @Param("doVreme") LocalDateTime doVreme);
}
