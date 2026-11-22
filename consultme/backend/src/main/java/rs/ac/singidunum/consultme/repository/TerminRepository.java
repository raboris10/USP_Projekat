package rs.ac.singidunum.consultme.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import rs.ac.singidunum.consultme.model.StatusTermina;
import rs.ac.singidunum.consultme.model.Termin;

public interface TerminRepository extends JpaRepository<Termin, Long> {

	// Da li za predmet postoji bar jedan termin (tada predmet ne sme da se obriše)
	boolean existsByPredmetId(Long predmetId);

	// Svi termini jednog profesora, hronološki
	List<Termin> findByProfesorIdOrderByPocetakAsc(Long profesorId);

	// Pretraga AKTIVNIH termina za studente. Filteri koji su null se preskaču
	// (":profesorId is null or ..." -> ako filter nije zadat, uslov je uvek tačan).
	// LEFT JOIN na predmet -> da ne izgubimo opšte termine (bez predmeta).
	@Query("""
			select t from Termin t
			left join t.predmet p
			where t.status = rs.ac.singidunum.consultme.model.StatusTermina.AKTIVAN
			  and t.pocetak >= :od
			  and t.pocetak < :doVremena
			  and (:profesorId is null or t.profesor.id = :profesorId)
			  and (:predmetId is null or p.id = :predmetId)
			order by t.pocetak
			""")
	List<Termin> pretrazi(
			@Param("profesorId") Long profesorId,
			@Param("predmetId") Long predmetId,
			@Param("od") LocalDateTime od,
			@Param("doVremena") LocalDateTime doVremena);

	// Koliko AKTIVNIH termina profesora se preklapa sa intervalom [pocetak, kraj).
	// Dva intervala se preklapaju ako jedan počinje pre nego što se drugi završi, i obrnuto.
	// Kraj termina nije kolona u bazi, pa ga računamo: pocetak + trajanje_min minuta.
	// Zato je ovo "native" (pravi MySQL) upit - JPQL nema DATE_ADD.
	// izuzetiId -> termin koji se menja ne poredimo sa samim sobom (za novi termin šaljemo -1).
	@Query(value = """
			select count(*) from termin
			where profesor_id = :profesorId
			  and status = 'AKTIVAN'
			  and id <> :izuzetiId
			  and pocetak < :kraj
			  and date_add(pocetak, interval trajanje_min minute) > :pocetak
			""", nativeQuery = true)
	long brojPreklapanja(
			@Param("profesorId") Long profesorId,
			@Param("pocetak") LocalDateTime pocetak,
			@Param("kraj") LocalDateTime kraj,
			@Param("izuzetiId") Long izuzetiId);

	// Učitava termin i ZAKLJUČAVA njegov red u bazi do kraja transakcije (SELECT ... FOR UPDATE).
	// Ako dva studenta u istom trenutku rezervišu poslednje mesto, drugi čeka da prvi završi,
	// pa tek onda broji slobodna mesta - i vidi da ih više nema.
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select t from Termin t where t.id = :id")
	Optional<Termin> zakljucajZaRezervaciju(@Param("id") Long id);

	// Da li profesor ima ijedan termin (za proveru pre promene uloge)
	boolean existsByProfesorId(Long profesorId);

	// Da li profesor ima termin sa datim statusom posle datog trenutka
	// (npr. AKTIVAN termin u budućnosti -> profesor ne sme da se deaktivira)
	boolean existsByProfesorIdAndStatusAndPocetakAfter(Long profesorId, StatusTermina status, LocalDateTime posle);

}
