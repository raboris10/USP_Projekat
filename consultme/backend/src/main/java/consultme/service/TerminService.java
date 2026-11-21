package consultme.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import consultme.exception.ConsultMeException;
import consultme.model.Nastavnik;
import consultme.model.Predmet;
import consultme.model.Rezervacija;
import consultme.model.StatusRezervacije;
import consultme.model.StatusTermina;
import consultme.model.Termin;
import consultme.model.TipNotifikacije;
import consultme.repository.NastavnikRepository;
import consultme.repository.PredmetRepository;
import consultme.repository.TerminRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class TerminService {

	@Autowired
	private TerminRepository terminRepository;
	@Autowired
	private NastavnikRepository nastavnikRepository;
	@Autowired
	private PredmetRepository predmetRepository;
	@Autowired
	private NotifikacijaService notifikacijaService;

	public Termin getById(Long id) {
		return terminRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Termin sa ID " + id + " nije pronadjen"));
	}

	// pretraga termina po nastavniku, predmetu i periodu (podrazumevano narednih 90 dana)
	public List<Termin> pretrazi(Long nastavnikId, Long predmetId, LocalDateTime od, LocalDateTime doVremena) {
		LocalDateTime odVreme = od != null ? od : LocalDateTime.now();
		LocalDateTime doVreme = doVremena != null ? doVremena : odVreme.plusDays(90);
		return terminRepository.pretrazi(nastavnikId, predmetId, odVreme, doVreme);
	}

	public List<Termin> getMoji(String emailNastavnika) {
		Nastavnik nastavnik = nadjiNastavnika(emailNastavnika);
		return terminRepository.findByNastavnikIdOrderByPocetak(nastavnik.getId());
	}

	// definisiTermin: nastavnik dolazi iz tokena, a predmeti se salju kao [{"id":1}]
	@Transactional
	public Termin create(String emailNastavnika, Termin podaci) {
		Nastavnik nastavnik = nadjiNastavnika(emailNastavnika);
		proveriVreme(podaci.getPocetak(), podaci.getKraj());

		Termin termin = new Termin();
		termin.setNastavnik(nastavnik);
		termin.setPocetak(podaci.getPocetak());
		termin.setKraj(podaci.getKraj());
		termin.setKapacitet(podaci.getKapacitet());
		termin.setMesto(podaci.getMesto());
		termin.setStatus(StatusTermina.OTVOREN);
		termin.setPredmeti(ucitajPredmete(podaci.getPredmeti()));
		return terminRepository.save(termin);
	}

	// pomeriTermin: menja vreme (i opciono mesto) i obavestava studente sa aktivnom rezervacijom
	@Transactional
	public Termin pomeri(String emailNastavnika, Long id, LocalDateTime pocetak, LocalDateTime kraj, String mesto) {
		Termin termin = nadjiSvojTermin(emailNastavnika, id);
		if (termin.getStatus() == StatusTermina.OTKAZAN) {
			throw new ConsultMeException("Otkazan termin se ne moze pomeriti");
		}
		proveriVreme(pocetak, kraj);
		termin.setPocetak(pocetak);
		termin.setKraj(kraj);
		if (mesto != null && !mesto.isBlank()) {
			termin.setMesto(mesto);
		}
		for (Rezervacija r : termin.getRezervacije()) {
			if (r.getStatus() == StatusRezervacije.AKTIVNA) {
				notifikacijaService.posalji(r, TipNotifikacije.IZMENA);
			}
		}
		return terminRepository.save(termin);
	}

	// otkaziTermin: otkazuje termin i sve njegove aktivne rezervacije
	@Transactional
	public Termin otkazi(String emailNastavnika, Long id) {
		Termin termin = nadjiSvojTermin(emailNastavnika, id);
		if (termin.getStatus() == StatusTermina.OTKAZAN) {
			throw new ConsultMeException("Termin je vec otkazan");
		}
		termin.setStatus(StatusTermina.OTKAZAN);
		for (Rezervacija r : termin.getRezervacije()) {
			if (r.getStatus() == StatusRezervacije.AKTIVNA) {
				r.otkazi();
				notifikacijaService.posalji(r, TipNotifikacije.OTKAZIVANJE);
			}
		}
		return terminRepository.save(termin);
	}

	private Nastavnik nadjiNastavnika(String email) {
		return nastavnikRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("Nastavnik nije pronadjen"));
	}

	// nastavnik sme da menja samo svoje termine
	private Termin nadjiSvojTermin(String emailNastavnika, Long id) {
		Termin termin = getById(id);
		if (!termin.getNastavnik().getEmail().equals(emailNastavnika)) {
			throw new AccessDeniedException("Mozete menjati samo svoje termine");
		}
		return termin;
	}

	private void proveriVreme(LocalDateTime pocetak, LocalDateTime kraj) {
		if (pocetak == null || kraj == null) {
			throw new ConsultMeException("Pocetak i kraj termina su obavezni");
		}
		if (!kraj.isAfter(pocetak)) {
			throw new ConsultMeException("Kraj termina mora biti posle pocetka");
		}
		if (!pocetak.isAfter(LocalDateTime.now())) {
			throw new ConsultMeException("Termin mora biti u buducnosti");
		}
	}

	private Set<Predmet> ucitajPredmete(Set<Predmet> zahtevani) {
		Set<Predmet> rezultat = new HashSet<>();
		if (zahtevani == null) {
			return rezultat;
		}
		for (Predmet p : zahtevani) {
			if (p.getId() == null) {
				throw new ConsultMeException("Svaki predmet mora imati id");
			}
			rezultat.add(predmetRepository.findById(p.getId())
					.orElseThrow(() -> new EntityNotFoundException("Predmet sa ID " + p.getId() + " nije pronadjen")));
		}
		return rezultat;
	}
}
