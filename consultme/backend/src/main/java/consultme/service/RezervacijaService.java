package consultme.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import consultme.exception.ConsultMeException;
import consultme.model.Rezervacija;
import consultme.model.StatusRezervacije;
import consultme.model.StatusTermina;
import consultme.model.Student;
import consultme.model.Termin;
import consultme.model.TipNotifikacije;
import consultme.repository.RezervacijaRepository;
import consultme.repository.StudentRepository;
import consultme.repository.TerminRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class RezervacijaService {

	// najvise aktivnih (buducih) rezervacija po studentu
	static final int MAX_AKTIVNIH_REZERVACIJA = 2;
	// rezervacija se moze otkazati najkasnije toliko sati pre pocetka termina
	static final long MIN_SATI_ZA_OTKAZIVANJE = 2;

	@Autowired
	private RezervacijaRepository rezervacijaRepository;
	@Autowired
	private TerminRepository terminRepository;
	@Autowired
	private StudentRepository studentRepository;
	@Autowired
	private NotifikacijaService notifikacijaService;

	public Rezervacija getById(Long id) {
		return rezervacijaRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Rezervacija sa ID " + id + " nije pronadjena"));
	}

	public List<Rezervacija> getMoje(String emailStudenta) {
		Student student = nadjiStudenta(emailStudenta);
		return rezervacijaRepository.findByStudentIdOrderByTerminPocetakDesc(student.getId());
	}

	// nastavnik vidi rezervacije svog termina
	public List<Rezervacija> getByTermin(String emailNastavnika, Long terminId) {
		Termin termin = terminRepository.findById(terminId)
				.orElseThrow(() -> new EntityNotFoundException("Termin nije pronadjen"));
		if (!termin.getNastavnik().getEmail().equals(emailNastavnika)) {
			throw new AccessDeniedException("Mozete videti samo rezervacije svojih termina");
		}
		return rezervacijaRepository.findByTerminIdOrderByKreiranaAsc(terminId);
	}

	// student rezervise termin
	@Transactional
	public Rezervacija rezervisi(String emailStudenta, Long terminId, String tema) {
		Student student = nadjiStudenta(emailStudenta);
		// zakljucan red termina: dva studenta ne mogu istovremeno uzeti poslednje mesto
		Termin termin = terminRepository.findByIdForUpdate(terminId)
				.orElseThrow(() -> new EntityNotFoundException("Termin nije pronadjen"));
		LocalDateTime sada = LocalDateTime.now();

		if (termin.getStatus() == StatusTermina.OTKAZAN) {
			throw new ConsultMeException("Termin je otkazan");
		}
		if (!termin.getPocetak().isAfter(sada)) {
			throw new ConsultMeException("Ne moze se rezervisati termin u proslosti");
		}
		if (!termin.imaSlobodnoMesto()) {
			throw new ConsultMeException("Termin je popunjen");
		}
		// da ne dodje do duple rezervacije
		if (rezervacijaRepository.existsByStudentIdAndTerminIdAndStatus(student.getId(), termin.getId(),
				StatusRezervacije.AKTIVNA)) {
			throw new ConsultMeException("Vec ste rezervisali ovaj termin");
		}
		long aktivne = rezervacijaRepository.countByStudentIdAndStatusAndTerminPocetakAfter(student.getId(),
				StatusRezervacije.AKTIVNA, sada);
		if (aktivne >= MAX_AKTIVNIH_REZERVACIJA) {
			throw new ConsultMeException("Mozete imati najvise " + MAX_AKTIVNIH_REZERVACIJA + " aktivne rezervacije");
		}

		Rezervacija rezervacija = new Rezervacija();
		rezervacija.setStudent(student);
		rezervacija.setTermin(termin);
		rezervacija.setTema(tema);
		rezervacija.setStatus(StatusRezervacije.AKTIVNA);
		rezervacija.setKreirana(sada);
		rezervacijaRepository.save(rezervacija);

		// azuriranje popunjenosti termina
		termin.getRezervacije().add(rezervacija);
		termin.azurirajStatus();
		terminRepository.save(termin);

		notifikacijaService.posalji(rezervacija, TipNotifikacije.POTVRDA);
		return rezervacija;
	}

	// student otkazuje svoju rezervaciju (najkasnije 2 sata pre termina), mesto se oslobadja
	@Transactional
	public Rezervacija otkazi(String emailStudenta, Long rezervacijaId) {
		Rezervacija rezervacija = getById(rezervacijaId);
		if (!rezervacija.getStudent().getEmail().equals(emailStudenta)) {
			throw new AccessDeniedException("Mozete otkazati samo svoje rezervacije");
		}
		if (rezervacija.getStatus() != StatusRezervacije.AKTIVNA) {
			throw new ConsultMeException("Rezervacija je vec otkazana");
		}
		Termin termin = rezervacija.getTermin();
		if (termin.getPocetak().isBefore(LocalDateTime.now().plusHours(MIN_SATI_ZA_OTKAZIVANJE))) {
			throw new ConsultMeException(
					"Rezervacija se moze otkazati najkasnije " + MIN_SATI_ZA_OTKAZIVANJE + " sata pre termina");
		}

		rezervacija.otkazi();
		rezervacijaRepository.save(rezervacija);

		termin.azurirajStatus();
		terminRepository.save(termin);

		notifikacijaService.posalji(rezervacija, TipNotifikacije.OTKAZIVANJE);
		return rezervacija;
	}

	private Student nadjiStudenta(String email) {
		return studentRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("Student nije pronadjen"));
	}
}
