package rs.ac.singidunum.consultme.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.ac.singidunum.consultme.dto.PredmetDto;
import rs.ac.singidunum.consultme.dto.PredmetRequest;
import rs.ac.singidunum.consultme.exception.PoslovnaGreskaException;
import rs.ac.singidunum.consultme.exception.ResourceNotFoundException;
import rs.ac.singidunum.consultme.model.Katedra;
import rs.ac.singidunum.consultme.model.Korisnik;
import rs.ac.singidunum.consultme.model.Predmet;
import rs.ac.singidunum.consultme.model.Uloga;
import rs.ac.singidunum.consultme.repository.KatedraRepository;
import rs.ac.singidunum.consultme.repository.KorisnikRepository;
import rs.ac.singidunum.consultme.repository.PredmetRepository;
import rs.ac.singidunum.consultme.repository.TerminRepository;

// Poslovna logika za predmete (uključujući dodelu profesora predmetu)
@Service
public class PredmetService {

	private final PredmetRepository predmetRepository;
	private final KatedraRepository katedraRepository;
	private final KorisnikRepository korisnikRepository;
	private final TerminRepository terminRepository;

	public PredmetService(PredmetRepository predmetRepository, KatedraRepository katedraRepository,
			KorisnikRepository korisnikRepository, TerminRepository terminRepository) {
		this.predmetRepository = predmetRepository;
		this.katedraRepository = katedraRepository;
		this.korisnikRepository = korisnikRepository;
		this.terminRepository = terminRepository;
	}

	// Svi predmeti ili samo predmeti jedne katedre (ako je katedraId prosleđen)
	@Transactional(readOnly = true)
	public List<PredmetDto> findAll(Long katedraId) {
		List<Predmet> predmeti = (katedraId == null)
				? predmetRepository.findAll(Sort.by("naziv"))
				: predmetRepository.findByKatedraIdOrderByNazivAsc(katedraId);

		return predmeti.stream()
				.map(PredmetDto::od)
				.toList();
	}

	@Transactional(readOnly = true)
	public PredmetDto findById(Long id) {
		return PredmetDto.od(nadji(id));
	}

	@Transactional
	public PredmetDto create(PredmetRequest zahtev) {
		String sifra = zahtev.sifra().trim().toUpperCase();

		if (predmetRepository.existsBySifraIgnoreCase(sifra)) {
			throw new PoslovnaGreskaException("Predmet sa šifrom \"" + sifra + "\" već postoji.");
		}

		Predmet predmet = new Predmet(zahtev.naziv().trim(), sifra, nadjiKatedru(zahtev.katedraId()));
		predmet.setProfesori(nadjiProfesore(zahtev.profesorIds()));

		return PredmetDto.od(predmetRepository.save(predmet));
	}

	@Transactional
	public PredmetDto update(Long id, PredmetRequest zahtev) {
		Predmet predmet = nadji(id);
		String sifra = zahtev.sifra().trim().toUpperCase();

		if (predmetRepository.existsBySifraIgnoreCaseAndIdNot(sifra, id)) {
			throw new PoslovnaGreskaException("Predmet sa šifrom \"" + sifra + "\" već postoji.");
		}

		predmet.setNaziv(zahtev.naziv().trim());
		predmet.setSifra(sifra);
		predmet.setKatedra(nadjiKatedru(zahtev.katedraId()));
		// Lista profesora se zamenjuje celom novom listom
		// (Hibernate sam doda/obriše redove u tabeli profesor_predmet)
		predmet.setProfesori(nadjiProfesore(zahtev.profesorIds()));

		return PredmetDto.od(predmet);
	}

	@Transactional
	public void delete(Long id) {
		Predmet predmet = nadji(id);

		if (terminRepository.existsByPredmetId(id)) {
			throw new PoslovnaGreskaException("Predmet ne može da se obriše jer za njega postoje termini.");
		}

		// Redovi iz profesor_predmet za ovaj predmet brišu se automatski
		predmetRepository.delete(predmet);
	}

	// ----- pomoćne metode -----

	private Predmet nadji(Long id) {
		return predmetRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Predmet nije pronađen."));
	}

	private Katedra nadjiKatedru(Long katedraId) {
		return katedraRepository.findById(katedraId)
				.orElseThrow(() -> new ResourceNotFoundException("Katedra nije pronađena."));
	}

	// Učitava profesore po id-jevima i proverava da svi postoje i da su zaista profesori
	private Set<Korisnik> nadjiProfesore(List<Long> profesorIds) {
		if (profesorIds == null || profesorIds.isEmpty()) {
			return new HashSet<>();
		}

		Set<Long> trazeni = new HashSet<>(profesorIds);   // Set -> izbacuje duplikate
		List<Korisnik> nadjeni = korisnikRepository.findAllById(trazeni);

		if (nadjeni.size() != trazeni.size()) {
			throw new ResourceNotFoundException("Neki od izabranih profesora ne postoji.");
		}

		for (Korisnik korisnik : nadjeni) {
			if (korisnik.getUloga() != Uloga.PROFESOR) {
				throw new PoslovnaGreskaException(
						korisnik.getIme() + " " + korisnik.getPrezime() + " nije profesor.");
			}
		}

		return new HashSet<>(nadjeni);
	}

}
