package consultme.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import consultme.exception.ConsultMeException;
import consultme.model.Katedra;
import consultme.repository.KatedraRepository;
import consultme.repository.NastavnikRepository;
import consultme.repository.PredmetRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class KatedraService {

	@Autowired
	private KatedraRepository katedraRepository;
	@Autowired
	private NastavnikRepository nastavnikRepository;
	@Autowired
	private PredmetRepository predmetRepository;

	public List<Katedra> getAll() {
		return katedraRepository.findAll();
	}

	public Katedra getById(Long id) {
		return katedraRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Katedra sa ID " + id + " nije pronadjena"));
	}

	@Transactional
	public Katedra create(Katedra katedra) {
		katedra.setId(null);
		return katedraRepository.save(katedra);
	}

	@Transactional
	public Katedra update(Long id, Katedra podaci) {
		Katedra katedra = getById(id);
		katedra.setNaziv(podaci.getNaziv());
		return katedraRepository.save(katedra);
	}

	@Transactional
	public void delete(Long id) {
		Katedra katedra = getById(id);
		if (nastavnikRepository.existsByKatedraId(id) || predmetRepository.existsByKatedraId(id)) {
			throw new ConsultMeException("Katedra se ne moze obrisati jer ima nastavnike ili predmete");
		}
		katedraRepository.delete(katedra);
	}
}
