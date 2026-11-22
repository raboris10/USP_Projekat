package rs.ac.singidunum.consultme.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.ac.singidunum.consultme.dto.KatedraDto;
import rs.ac.singidunum.consultme.dto.KatedraRequest;
import rs.ac.singidunum.consultme.exception.PoslovnaGreskaException;
import rs.ac.singidunum.consultme.exception.ResourceNotFoundException;
import rs.ac.singidunum.consultme.model.Katedra;
import rs.ac.singidunum.consultme.repository.KatedraRepository;
import rs.ac.singidunum.consultme.repository.PredmetRepository;

// Poslovna logika za katedre
@Service
public class KatedraService {

	private final KatedraRepository katedraRepository;
	private final PredmetRepository predmetRepository;

	public KatedraService(KatedraRepository katedraRepository, PredmetRepository predmetRepository) {
		this.katedraRepository = katedraRepository;
		this.predmetRepository = predmetRepository;
	}

	// Sve katedre, abecedno po nazivu
	// @Transactional(readOnly = true) -> samo čitanje; veza sa bazom je otvorena dok traje metoda
	@Transactional(readOnly = true)
	public List<KatedraDto> findAll() {
		return katedraRepository.findAll(Sort.by("naziv")).stream()
				.map(KatedraDto::od)
				.toList();
	}

	@Transactional(readOnly = true)
	public KatedraDto findById(Long id) {
		return KatedraDto.od(nadji(id));
	}

	// @Transactional -> ako bilo šta pukne usred metode, ništa se ne upisuje u bazu
	@Transactional
	public KatedraDto create(KatedraRequest zahtev) {
		String naziv = zahtev.naziv().trim();

		if (katedraRepository.existsByNazivIgnoreCase(naziv)) {
			throw new PoslovnaGreskaException("Katedra sa nazivom \"" + naziv + "\" već postoji.");
		}

		Katedra katedra = katedraRepository.save(new Katedra(naziv));
		return KatedraDto.od(katedra);
	}

	@Transactional
	public KatedraDto update(Long id, KatedraRequest zahtev) {
		Katedra katedra = nadji(id);
		String naziv = zahtev.naziv().trim();

		if (katedraRepository.existsByNazivIgnoreCaseAndIdNot(naziv, id)) {
			throw new PoslovnaGreskaException("Katedra sa nazivom \"" + naziv + "\" već postoji.");
		}

		// Nije potreban save(): objekat je učitan u ovoj transakciji,
		// pa Hibernate sam upiše promenu u bazu na kraju metode.
		katedra.setNaziv(naziv);
		return KatedraDto.od(katedra);
	}

	@Transactional
	public void delete(Long id) {
		Katedra katedra = nadji(id);

		if (predmetRepository.existsByKatedraId(id)) {
			throw new PoslovnaGreskaException("Katedra ne može da se obriše jer ima predmete.");
		}

		katedraRepository.delete(katedra);
	}

	// Pomoćna metoda: učitaj katedru ili baci 404
	private Katedra nadji(Long id) {
		return katedraRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Katedra nije pronađena."));
	}

}
