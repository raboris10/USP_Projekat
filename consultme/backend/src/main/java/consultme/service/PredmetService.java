package consultme.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import consultme.exception.ConsultMeException;
import consultme.model.Katedra;
import consultme.model.Predmet;
import consultme.repository.KatedraRepository;
import consultme.repository.PredmetRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class PredmetService {

	@Autowired
	private PredmetRepository predmetRepository;
	@Autowired
	private KatedraRepository katedraRepository;

	public List<Predmet> getAll() {
		return predmetRepository.findAll();
	}

	public Predmet getById(Long id) {
		return predmetRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Predmet sa ID " + id + " nije pronadjen"));
	}

	public List<Predmet> getByKatedra(Long katedraId) {
		return predmetRepository.findByKatedraId(katedraId);
	}

	@Transactional
	public Predmet create(Long katedraId, Predmet predmet) {
		Katedra katedra = katedraRepository.findById(katedraId)
				.orElseThrow(() -> new EntityNotFoundException("Katedra nije pronadjena"));
		if (predmetRepository.existsBySifra(predmet.getSifra())) {
			throw new ConsultMeException("Predmet sa sifrom " + predmet.getSifra() + " vec postoji");
		}
		predmet.setId(null);
		predmet.setKatedra(katedra);
		return predmetRepository.save(predmet);
	}

	@Transactional
	public Predmet update(Long id, Predmet podaci) {
		Predmet predmet = getById(id);
		if (!predmet.getSifra().equals(podaci.getSifra()) && predmetRepository.existsBySifra(podaci.getSifra())) {
			throw new ConsultMeException("Predmet sa sifrom " + podaci.getSifra() + " vec postoji");
		}
		predmet.setSifra(podaci.getSifra());
		predmet.setNaziv(podaci.getNaziv());
		return predmetRepository.save(predmet);
	}

	@Transactional
	public void delete(Long id) {
		Predmet predmet = getById(id);
		if (!predmet.getTermini().isEmpty()) {
			throw new ConsultMeException("Predmet se ne moze obrisati jer postoje termini vezani za njega");
		}
		predmetRepository.delete(predmet);
	}
}
