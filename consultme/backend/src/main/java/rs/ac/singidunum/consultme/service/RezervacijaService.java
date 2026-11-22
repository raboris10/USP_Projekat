package rs.ac.singidunum.consultme.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.ac.singidunum.consultme.dto.RezervacijaDto;
import rs.ac.singidunum.consultme.dto.RezervacijaRequest;
import rs.ac.singidunum.consultme.dto.TerminDto;
import rs.ac.singidunum.consultme.exception.PoslovnaGreskaException;
import rs.ac.singidunum.consultme.exception.ResourceNotFoundException;
import rs.ac.singidunum.consultme.exception.ZabranjenPristupException;
import rs.ac.singidunum.consultme.model.Korisnik;
import rs.ac.singidunum.consultme.model.Rezervacija;
import rs.ac.singidunum.consultme.model.StatusRezervacije;
import rs.ac.singidunum.consultme.model.StatusTermina;
import rs.ac.singidunum.consultme.model.Termin;
import rs.ac.singidunum.consultme.model.TipNotifikacije;
import rs.ac.singidunum.consultme.repository.KorisnikRepository;
import rs.ac.singidunum.consultme.repository.RezervacijaRepository;
import rs.ac.singidunum.consultme.repository.TerminRepository;

// Poslovna logika za rezervacije (student se prijavljuje na termin i otkazuje prijavu)
@Service
public class RezervacijaService {

	private final RezervacijaRepository rezervacijaRepository;
	private final TerminRepository terminRepository;
	private final KorisnikRepository korisnikRepository;
	private final NotifikacijaService notifikacijaService;

	// Pravila iz application.properties (mogu da se promene bez menjanja koda)
	private final int najviseAktivnih;      // najviše aktivnih rezervacija po studentu
	private final int otkazivanjeSatiPre;   // najkasnije koliko sati pre termina sme da se otkaže

	public RezervacijaService(RezervacijaRepository rezervacijaRepository, TerminRepository terminRepository,
			KorisnikRepository korisnikRepository, NotifikacijaService notifikacijaService,
			@Value("${rezervacije.najvise-aktivnih}") int najviseAktivnih,
			@Value("${rezervacije.otkazivanje-sati-pre}") int otkazivanjeSatiPre) {
		this.rezervacijaRepository = rezervacijaRepository;
		this.terminRepository = terminRepository;
		this.korisnikRepository = korisnikRepository;
		this.notifikacijaService = notifikacijaService;
		this.najviseAktivnih = najviseAktivnih;
		this.otkazivanjeSatiPre = otkazivanjeSatiPre;
	}

	// =====================================================================
	// PREGLED
	// =====================================================================

	// Sve rezervacije prijavljenog studenta (i aktivne i otkazane), najnoviji termini prvi
	@Transactional(readOnly = true)
	public List<RezervacijaDto> moje(String emailStudenta) {
		Korisnik student = nadjiKorisnika(emailStudenta);

		return rezervacijaRepository.findByStudentIdOrderByTerminPocetakDesc(student.getId()).stream()
				.map(this::uDto)
				.toList();
	}

	// =====================================================================
	// REZERVISANJE
	// =====================================================================

	@Transactional
	public RezervacijaDto rezervisi(String emailStudenta, RezervacijaRequest zahtev) {
		Korisnik student = nadjiKorisnika(emailStudenta);
		LocalDateTime sada = LocalDateTime.now();

		// 1) Učitaj i zaključaj termin (zaštita poslednjeg slobodnog mesta - vidi TerminRepository)
		Termin termin = terminRepository.zakljucajZaRezervaciju(zahtev.terminId())
				.orElseThrow(() -> new ResourceNotFoundException("Termin nije pronađen."));

		// 2) Termin mora biti aktivan i u budućnosti
		if (termin.getStatus() == StatusTermina.OTKAZAN) {
			throw new PoslovnaGreskaException("Termin je otkazan.");
		}
		if (!termin.getPocetak().isAfter(sada)) {
			throw new PoslovnaGreskaException("Termin je već počeo ili je završen.");
		}

		// 3) Student ne sme dvaput na isti termin
		Rezervacija postojeca = rezervacijaRepository.findByStudentIdAndTerminId(student.getId(), termin.getId())
				.orElse(null);
		if (postojeca != null && postojeca.getStatus() == StatusRezervacije.AKTIVNA) {
			throw new PoslovnaGreskaException("Već ste prijavljeni na ovaj termin.");
		}

		// 4) Mora biti slobodnog mesta
		long zauzeto = rezervacijaRepository.countByTerminIdAndStatus(termin.getId(), StatusRezervacije.AKTIVNA);
		if (zauzeto >= termin.getKapacitet()) {
			throw new PoslovnaGreskaException("Termin je popunjen.");
		}

		// 5) Ograničenje broja aktivnih rezervacija po studentu
		if (rezervacijaRepository.brojAktivnihBuducih(student.getId(), sada) >= najviseAktivnih) {
			throw new PoslovnaGreskaException("Možete imati najviše " + najviseAktivnih
					+ " aktivne rezervacije. Otkažite neku ili sačekajte da se održi.");
		}

		// 6) Student ne sme imati drugi termin u isto vreme
		LocalDateTime kraj = termin.getPocetak().plusMinutes(termin.getTrajanjeMin());
		if (rezervacijaRepository.brojPreklapanjaStudenta(student.getId(), termin.getId(), termin.getPocetak(), kraj) > 0) {
			throw new PoslovnaGreskaException("Već imate rezervisan drugi termin u isto vreme.");
		}

		// 7) Sve je u redu. Ako je student ranije otkazao ovaj termin, aktiviramo staru rezervaciju
		//    (u bazi sme da postoji samo jedan red za par student-termin), inače pravimo novu.
		Rezervacija rezervacija;
		if (postojeca != null) {
			rezervacija = postojeca;
			rezervacija.setTema(zahtev.tema().trim());
			rezervacija.setStatus(StatusRezervacije.AKTIVNA);
			rezervacija.setDolazak(null);
			rezervacija.setKreirana(sada);
		} else {
			rezervacija = rezervacijaRepository.save(new Rezervacija(student, termin, zahtev.tema().trim()));
		}

		// 8) Obaveštenja: potvrda studentu, obaveštenje profesoru
		String vreme = termin.getPocetak().format(TerminService.FORMAT);

		notifikacijaService.posalji(student, TipNotifikacije.REZERVACIJA_POTVRDJENA,
				"Rezervisali ste termin konsultacija (" + TerminService.opisTermina(termin) + "): "
						+ vreme + ", " + termin.getMesto() + ".",
				termin);

		notifikacijaService.posalji(termin.getProfesor(), TipNotifikacije.NOVA_REZERVACIJA,
				"Nova rezervacija za Vaš termin " + vreme + ": " + student.getIme() + " " + student.getPrezime()
						+ ". Tema: " + rezervacija.getTema(),
				termin);

		return uDto(rezervacija);
	}

	// =====================================================================
	// OTKAZIVANJE
	// =====================================================================

	@Transactional
	public RezervacijaDto otkazi(String emailStudenta, Long rezervacijaId) {
		Rezervacija rezervacija = rezervacijaRepository.findById(rezervacijaId)
				.orElseThrow(() -> new ResourceNotFoundException("Rezervacija nije pronađena."));

		// Student sme da otkaže samo svoju rezervaciju
		if (!rezervacija.getStudent().getEmail().equals(emailStudenta)) {
			throw new ZabranjenPristupException("Možete otkazati samo svoje rezervacije.");
		}
		if (rezervacija.getStatus() == StatusRezervacije.OTKAZANA) {
			throw new PoslovnaGreskaException("Rezervacija je već otkazana.");
		}

		Termin termin = rezervacija.getTermin();
		if (!rokZaOtkazivanjeNijeProsao(termin)) {
			throw new PoslovnaGreskaException(
					"Rezervacija može da se otkaže najkasnije " + otkazivanjeSatiPre + " sata pre termina.");
		}

		rezervacija.setStatus(StatusRezervacije.OTKAZANA);

		Korisnik student = rezervacija.getStudent();
		notifikacijaService.posalji(termin.getProfesor(), TipNotifikacije.REZERVACIJA_OTKAZANA,
				"Otkazana rezervacija za Vaš termin " + termin.getPocetak().format(TerminService.FORMAT)
						+ ": " + student.getIme() + " " + student.getPrezime() + ".",
				termin);

		return uDto(rezervacija);
	}

	// =====================================================================
	// POMOĆNE METODE
	// =====================================================================

	private RezervacijaDto uDto(Rezervacija rezervacija) {
		Termin termin = rezervacija.getTermin();
		long prijavljeno = rezervacijaRepository.countByTerminIdAndStatus(termin.getId(), StatusRezervacije.AKTIVNA);

		boolean mozeDaSeOtkaze = rezervacija.getStatus() == StatusRezervacije.AKTIVNA
				&& termin.getStatus() == StatusTermina.AKTIVAN
				&& rokZaOtkazivanjeNijeProsao(termin);

		return RezervacijaDto.od(rezervacija, mozeDaSeOtkaze, TerminDto.od(termin, prijavljeno));
	}

	// Da li je do termina ostalo više od otkazivanjeSatiPre sati
	private boolean rokZaOtkazivanjeNijeProsao(Termin termin) {
		return LocalDateTime.now().isBefore(termin.getPocetak().minusHours(otkazivanjeSatiPre));
	}

	private Korisnik nadjiKorisnika(String email) {
		return korisnikRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Korisnik nije pronađen."));
	}

}
