package rs.ac.singidunum.consultme.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.ac.singidunum.consultme.model.Rezervacija;

// Osnovne CRUD metode (findAll, findById, save, delete) dobijamo automatski.
// Ovde ćemo dodavati sopstvene upite kada nam zatrebaju.
public interface RezervacijaRepository extends JpaRepository<Rezervacija, Long> {

}
