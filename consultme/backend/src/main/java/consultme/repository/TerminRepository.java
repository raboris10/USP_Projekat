package consultme.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import consultme.model.Termin;

public interface TerminRepository extends JpaRepository<Termin, Long> {

	List<Termin> findByNastavnikIdOrderByPocetak(Long nastavnikId);

	boolean existsByNastavnikId(Long nastavnikId);

	// zakljucava red termina do kraja transakcije (dva studenta ne mogu uzeti poslednje mesto istovremeno)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select t from Termin t where t.id = :id")
	Optional<Termin> findByIdForUpdate(@Param("id") Long id);

	// pretraga neotkazanih termina po nastavniku, predmetu i periodu
	@Query("select distinct t from Termin t left join t.predmeti p "
			+ "where t.status <> consultme.model.StatusTermina.OTKAZAN "
			+ "and (:nastavnikId is null or t.nastavnik.id = :nastavnikId) "
			+ "and (:predmetId is null or p.id = :predmetId) "
			+ "and t.pocetak >= :odVreme and t.pocetak <= :doVreme "
			+ "order by t.pocetak")
	List<Termin> pretrazi(@Param("nastavnikId") Long nastavnikId,
						  @Param("predmetId") Long predmetId,
						  @Param("odVreme") LocalDateTime odVreme,
						  @Param("doVreme") LocalDateTime doVreme);
}
