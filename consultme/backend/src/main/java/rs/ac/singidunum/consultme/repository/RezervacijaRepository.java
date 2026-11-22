package rs.ac.singidunum.consultme.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.ac.singidunum.consultme.model.Rezervacija;
import rs.ac.singidunum.consultme.model.StatusRezervacije;

public interface RezervacijaRepository extends JpaRepository<Rezervacija, Long> {

	// Broj rezervacija termina sa datim statusom (npr. AKTIVNA -> koliko je mesta zauzeto)
	long countByTerminIdAndStatus(Long terminId, StatusRezervacije status);

	// Rezervacije termina sa datim statusom (npr. svi aktivno prijavljeni - za notifikacije)
	List<Rezervacija> findByTerminIdAndStatus(Long terminId, StatusRezervacije status);

	// Sve rezervacije termina, redom kako su pravljene (lista prijavljenih za profesora)
	List<Rezervacija> findByTerminIdOrderByKreiranaAsc(Long terminId);

}
