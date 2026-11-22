package rs.ac.singidunum.consultme.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rs.ac.singidunum.consultme.model.Rezervacija;
import rs.ac.singidunum.consultme.model.StatusRezervacije;

public interface RezervacijaRepository extends JpaRepository<Rezervacija, Long> {

	// Broj rezervacija termina sa datim statusom (npr. AKTIVNA -> koliko je mesta zauzeto)
	long countByTerminIdAndStatus(Long terminId, StatusRezervacije status);

	// Rezervacije termina sa datim statusom (npr. svi aktivno prijavljeni - za notifikacije)
	List<Rezervacija> findByTerminIdAndStatus(Long terminId, StatusRezervacije status);

	// Sve rezervacije termina, redom kako su pravljene (lista prijavljenih za profesora)
	List<Rezervacija> findByTerminIdOrderByKreiranaAsc(Long terminId);

	// Rezervacija ovog studenta za ovaj termin (aktivna ili ranije otkazana), ako postoji.
	// U bazi može postojati najviše jedna - UNIQUE (student_id, termin_id).
	Optional<Rezervacija> findByStudentIdAndTerminId(Long studentId, Long terminId);

	// Sve rezervacije studenta, najnoviji termini prvi
	// (OrderByTerminPocetak -> sortira po polju pocetak iz povezanog termina)
	List<Rezervacija> findByStudentIdOrderByTerminPocetakDesc(Long studentId);

	// Broj aktivnih rezervacija studenta za termine koji tek dolaze (za ograničenje broja rezervacija)
	@Query("""
			select count(r) from Rezervacija r
			where r.student.id = :studentId
			  and r.status = rs.ac.singidunum.consultme.model.StatusRezervacije.AKTIVNA
			  and r.termin.pocetak > :sada
			""")
	long brojAktivnihBuducih(@Param("studentId") Long studentId, @Param("sada") LocalDateTime sada);

	// Koliko aktivnih rezervacija studenta je za termine koji se preklapaju sa intervalom [pocetak, kraj).
	// Ista logika kao TerminRepository.brojPreklapanja, samo za studenta.
	@Query(value = """
			select count(*) from rezervacija r
			join termin t on t.id = r.termin_id
			where r.student_id = :studentId
			  and r.status = 'AKTIVNA'
			  and t.status = 'AKTIVAN'
			  and t.id <> :terminId
			  and t.pocetak < :kraj
			  and date_add(t.pocetak, interval t.trajanje_min minute) > :pocetak
			""", nativeQuery = true)
	long brojPreklapanjaStudenta(
			@Param("studentId") Long studentId,
			@Param("terminId") Long terminId,
			@Param("pocetak") LocalDateTime pocetak,
			@Param("kraj") LocalDateTime kraj);

}
