package consultme.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import consultme.exception.ConsultMeException;
import consultme.model.Katedra;
import consultme.model.Nastavnik;
import consultme.repository.KatedraRepository;
import consultme.repository.KorisnikRepository;
import consultme.repository.NastavnikRepository;
import consultme.repository.TerminRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class NastavnikService {

	@Autowired
	private NastavnikRepository nastavnikRepository;
	@Autowired
	private KatedraRepository katedraRepository;
	@Autowired
	private KorisnikRepository korisnikRepository;
	@Autowired
	private TerminRepository terminRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	public List<Nastavnik> getAll() {
		return nastavnikRepository.findAll();
	}

	public Nastavnik getById(Long id) {
		return nastavnikRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Nastavnik sa ID " + id + " nije pronadjen"));
	}

	public List<Nastavnik> getByKatedra(Long katedraId) {
		return nastavnikRepository.findByKatedraId(katedraId);
	}

	public List<Nastavnik> pretraziPoImenu(String ime) {
		return nastavnikRepository.pretraziPoImenu(ime);
	}

	// admin dodaje nastavnika na katedru i zadaje pocetnu lozinku
	@Transactional
	public Nastavnik create(Long katedraId, Nastavnik nastavnik) {
		Katedra katedra = katedraRepository.findById(katedraId)
				.orElseThrow(() -> new EntityNotFoundException("Katedra nije pronadjena"));
		proveriEmail(nastavnik.getEmail(), null);
		if (nastavnik.getLozinka() == null || nastavnik.getLozinka().isBlank()) {
			throw new ConsultMeException("Lozinka je obavezna");
		}
		nastavnik.setId(null);
		nastavnik.setKatedra(katedra);
		nastavnik.setLozinka(passwordEncoder.encode(nastavnik.getLozinka()));
		return nastavnikRepository.save(nastavnik);
	}

	@Transactional
	public Nastavnik update(Long id, Nastavnik podaci) {
		Nastavnik nastavnik = getById(id);
		proveriEmail(podaci.getEmail(), id);
		nastavnik.setIme(podaci.getIme());
		nastavnik.setPrezime(podaci.getPrezime());
		nastavnik.setEmail(podaci.getEmail());
		nastavnik.setZvanje(podaci.getZvanje());
		// lozinka se menja samo ako je poslata nova
		if (podaci.getLozinka() != null && !podaci.getLozinka().isBlank()) {
			nastavnik.setLozinka(passwordEncoder.encode(podaci.getLozinka()));
		}
		return nastavnikRepository.save(nastavnik);
	}

	// prebacivanje nastavnika na drugu katedru
	@Transactional
	public Nastavnik promeniKatedru(Long id, Long katedraId) {
		Nastavnik nastavnik = getById(id);
		Katedra katedra = katedraRepository.findById(katedraId)
				.orElseThrow(() -> new EntityNotFoundException("Katedra nije pronadjena"));
		nastavnik.setKatedra(katedra);
		return nastavnikRepository.save(nastavnik);
	}

	@Transactional
	public void delete(Long id) {
		Nastavnik nastavnik = getById(id);
		if (terminRepository.existsByNastavnikId(id)) {
			throw new ConsultMeException("Nastavnik se ne moze obrisati jer ima objavljene termine");
		}
		nastavnikRepository.delete(nastavnik);
	}

	private void proveriEmail(String email, Long idTrenutnog) {
		korisnikRepository.findByEmail(email).ifPresent(k -> {
			if (idTrenutnog == null || !k.getId().equals(idTrenutnog)) {
				throw new ConsultMeException("Korisnik sa emailom " + email + " vec postoji");
			}
		});
	}
}
