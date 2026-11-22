package rs.ac.singidunum.consultme.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.ac.singidunum.consultme.dto.PrijavljeniStudentDto;
import rs.ac.singidunum.consultme.dto.TerminDto;
import rs.ac.singidunum.consultme.dto.TerminIzmenaRequest;
import rs.ac.singidunum.consultme.dto.TerminRequest;
import rs.ac.singidunum.consultme.exception.PoslovnaGreskaException;
import rs.ac.singidunum.consultme.exception.ResourceNotFoundException;
import rs.ac.singidunum.consultme.exception.ZabranjenPristupException;
import rs.ac.singidunum.consultme.model.Korisnik;
import rs.ac.singidunum.consultme.model.Predmet;
import rs.ac.singidunum.consultme.model.Rezervacija;
import rs.ac.singidunum.consultme.model.StatusRezervacije;
import rs.ac.singidunum.consultme.model.StatusTermina;
import rs.ac.singidunum.consultme.model.Termin;
import rs.ac.singidunum.consultme.model.TipNotifikacije;
import rs.ac.singidunum.consultme.repository.KorisnikRepository;
import rs.ac.singidunum.consultme.repository.PredmetRepository;
import rs.ac.singidunum.consultme.repository.RezervacijaRepository;
import rs.ac.singidunum.consultme.repository.TerminRepository;

// Poslovna logika za termine konsultacija
@Service
public class TerminService {

	// Ponavljajući termin: najviše ovoliko nedelja unapred (otprilike jedan semestar)
	private static final int NAJVISE_PONAVLJANJA = 26;

	// Kako se datum i vreme ispisuju u porukama, npr. "01.12.2026. u 12:00"
	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy. 'u' HH:mm");

	private final TerminRepository terminRepository;
	private final RezervacijaRepository rezervacijaRepository;
	private final KorisnikRepository korisnikRepository;
	private final PredmetRepository predmetRepository;
	private final NotifikacijaService notifikacijaService;

	public TerminService(TerminRepository terminRepository, RezervacijaRepository rezervacijaRepository,
			KorisnikRepository korisnikRepository, PredmetRepository predmetRepository,
			NotifikacijaService notifikacijaService) {
		this.terminRepository = terminRepository;
		this.rezervacijaRepository = rezervacijaRepository;
		this.korisnikRepository = korisnikRepository;
		this.predmetRepository = predmetRepository;
		this.notifikacijaService = notifikacijaService;
	}

	// =====================================================================
	// PREGLED
	// =====================================================================

	// Pretraga za studente: samo AKTIVNI termini koji tek dolaze.
	// Svi filteri su opcioni; bez datuma "do" gleda se godinu dana unapred.
	@Transactional(readOnly = true)
	public List<TerminDto> pretrazi(Long profesorId, Long predmetId, LocalDate odDatuma, LocalDate doDatuma) {
		LocalDateTime sada = LocalDateTime.now();

		// od: zadati datum, ali nikad pre "sada" (prošli termini se ne nude)
		LocalDateTime od = (odDatuma != null && odDatuma.atStartOfDay().isAfter(sada))
				? odDatuma.atStartOfDay()
				: sada;
		// do: kraj zadatog dana (zato +1 dan), ili godinu dana unapred
		LocalDateTime doVremena = (doDatuma != null)
				? doDatuma.plusDays(1).atStartOfDay()
				: sada.plusYears(1);

		return terminRepository.pretrazi(profesorId, predmetId, od, doVremena).stream()
				.map(this::uDto)
				.toList();
	}

	// Svi termini prijavljenog profesora (i prošli i otkazani), hronološki
	@Transactional(readOnly = true)
	public List<TerminDto> moji(String emailProfesora) {
		Korisnik profesor = nadjiKorisnika(emailProfesora);

		return terminRepository.findByProfesorIdOrderByPocetakAsc(profesor.getId()).stream()
				.map(this::uDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public TerminDto findById(Long id) {
		return uDto(nadji(id));
	}

	// Lista prijavljenih studenata na terminu - sme da vidi samo profesor čiji je termin
	@Transactional(readOnly = true)
	public List<PrijavljeniStudentDto> prijavljeni(String emailProfesora, Long terminId) {
		Termin termin = nadjiSvoj(emailProfesora, terminId);

		return rezervacijaRepository.findByTerminIdOrderByKreiranaAsc(termin.getId()).stream()
				.map(PrijavljeniStudentDto::od)
				.toList();
	}

	// =====================================================================
	// PRAVLJENJE
	// =====================================================================

	// Pravi jedan termin, ili više njih ako je zadato ponavljanjeDo (svake nedelje u isto vreme).
	// @Transactional -> ako se BILO KOJI od njih preklapa sa postojećim, ne pravi se nijedan.
	@Transactional
	public List<TerminDto> create(String emailProfesora, TerminRequest zahtev) {
		Korisnik profesor = nadjiKorisnika(emailProfesora);
		Predmet predmet = nadjiPredmetProfesora(zahtev.predmetId(), profesor);
		String mesto = zahtev.mesto().trim();

		List<LocalDateTime> pocetci = izracunajPocetke(zahtev.pocetak(), zahtev.ponavljanjeDo());

		List<TerminDto> napravljeni = new ArrayList<>();
		for (LocalDateTime pocetak : pocetci) {
			proveriPreklapanje(profesor.getId(), pocetak, zahtev.trajanjeMin(), -1L);

			Termin termin = new Termin(profesor, predmet, pocetak, zahtev.trajanjeMin(), mesto, zahtev.kapacitet());
			napravljeni.add(TerminDto.od(terminRepository.save(termin), 0));   // nov termin nema prijavljenih
		}

		return napravljeni;
	}

	// =====================================================================
	// IZMENA (POMERANJE) I OTKAZIVANJE
	// =====================================================================

	@Transactional
	public TerminDto update(String emailProfesora, Long id, TerminIzmenaRequest zahtev) {
		Termin termin = nadjiSvoj(emailProfesora, id);
		proveriDaMozeDaSeMenja(termin);

		long prijavljeno = brojPrijavljenih(termin);
		if (zahtev.kapacitet() < prijavljeno) {
			throw new PoslovnaGreskaException(
					"Kapacitet ne može biti manji od broja prijavljenih studenata (" + prijavljeno + ").");
		}

		proveriPreklapanje(termin.getProfesor().getId(), zahtev.pocetak(), zahtev.trajanjeMin(), termin.getId());

		// Studente obaveštavamo samo ako se promenilo nešto što ih zanima (vreme, trajanje, mesto).
		// Promena samo kapaciteta ih ne pogađa.
		String mesto = zahtev.mesto().trim();
		boolean promenjenoZaStudente = !termin.getPocetak().equals(zahtev.pocetak())
				|| !termin.getTrajanjeMin().equals(zahtev.trajanjeMin())
				|| !termin.getMesto().equals(mesto);

		termin.setPocetak(zahtev.pocetak());
		termin.setTrajanjeMin(zahtev.trajanjeMin());
		termin.setMesto(mesto);
		termin.setKapacitet(zahtev.kapacitet());

		if (promenjenoZaStudente) {
			String poruka = "Termin konsultacija (" + opisTermina(termin) + ") je pomeren: "
					+ termin.getPocetak().format(FORMAT) + ", " + termin.getMesto() + ".";

			for (Rezervacija rezervacija : aktivneRezervacije(termin)) {
				notifikacijaService.posalji(rezervacija.getStudent(), TipNotifikacije.TERMIN_IZMENJEN, poruka, termin);
			}
		}

		return TerminDto.od(termin, prijavljeno);
	}

	// Otkazuje termin i sve njegove aktivne rezervacije; prijavljeni studenti dobijaju obaveštenje.
	// Termin se NE briše iz baze - ostaje zbog istorije (evidencija, statistika).
	@Transactional
	public TerminDto otkazi(String emailProfesora, Long id) {
		Termin termin = nadjiSvoj(emailProfesora, id);
		proveriDaMozeDaSeMenja(termin);

		termin.setStatus(StatusTermina.OTKAZAN);

		String poruka = "Termin konsultacija (" + opisTermina(termin) + ") zakazan za "
				+ termin.getPocetak().format(FORMAT) + " je otkazan.";

		for (Rezervacija rezervacija : aktivneRezervacije(termin)) {
			rezervacija.setStatus(StatusRezervacije.OTKAZANA);
			notifikacijaService.posalji(rezervacija.getStudent(), TipNotifikacije.TERMIN_OTKAZAN, poruka, termin);
		}

		return TerminDto.od(termin, 0);   // posle otkazivanja nema aktivno prijavljenih
	}

	// =====================================================================
	// POMOĆNE METODE
	// =====================================================================

	private TerminDto uDto(Termin termin) {
		return TerminDto.od(termin, brojPrijavljenih(termin));
	}

	private long brojPrijavljenih(Termin termin) {
		return rezervacijaRepository.countByTerminIdAndStatus(termin.getId(), StatusRezervacije.AKTIVNA);
	}

	private List<Rezervacija> aktivneRezervacije(Termin termin) {
		return rezervacijaRepository.findByTerminIdAndStatus(termin.getId(), StatusRezervacije.AKTIVNA);
	}

	private Termin nadji(Long id) {
		return terminRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Termin nije pronađen."));
	}

	private Korisnik nadjiKorisnika(String email) {
		return korisnikRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Korisnik nije pronađen."));
	}

	// Termin, ali samo ako pripada ovom profesoru
	private Termin nadjiSvoj(String emailProfesora, Long id) {
		Termin termin = nadji(id);
		if (!termin.getProfesor().getEmail().equals(emailProfesora)) {
			throw new ZabranjenPristupException("Možete menjati i pregledati samo svoje termine.");
		}
		return termin;
	}

	// Predmet je opcion; ako je zadat, mora da postoji i profesor mora da ga drži
	private Predmet nadjiPredmetProfesora(Long predmetId, Korisnik profesor) {
		if (predmetId == null) {
			return null;
		}

		Predmet predmet = predmetRepository.findById(predmetId)
				.orElseThrow(() -> new ResourceNotFoundException("Predmet nije pronađen."));

		boolean drziPredmet = predmet.getProfesori().stream()
				.anyMatch(p -> p.getId().equals(profesor.getId()));
		if (!drziPredmet) {
			throw new PoslovnaGreskaException("Ne držite predmet " + predmet.getNaziv() + ".");
		}

		return predmet;
	}

	// Jedan početak, ili isti dan i vreme svake nedelje do datuma ponavljanjeDo (uključujući i taj dan)
	private List<LocalDateTime> izracunajPocetke(LocalDateTime prvi, LocalDate ponavljanjeDo) {
		List<LocalDateTime> pocetci = new ArrayList<>();
		pocetci.add(prvi);

		if (ponavljanjeDo == null) {
			return pocetci;
		}

		if (ponavljanjeDo.isBefore(prvi.toLocalDate())) {
			throw new PoslovnaGreskaException("Datum do kog se termin ponavlja mora biti posle prvog termina.");
		}

		LocalDateTime sledeci = prvi.plusWeeks(1);
		while (!sledeci.toLocalDate().isAfter(ponavljanjeDo)) {
			pocetci.add(sledeci);
			sledeci = sledeci.plusWeeks(1);
		}

		if (pocetci.size() > NAJVISE_PONAVLJANJA) {
			throw new PoslovnaGreskaException(
					"Termin može da se ponovi najviše " + NAJVISE_PONAVLJANJA + " puta (otprilike jedan semestar).");
		}

		return pocetci;
	}

	private void proveriPreklapanje(Long profesorId, LocalDateTime pocetak, int trajanjeMin, Long izuzetiId) {
		LocalDateTime kraj = pocetak.plusMinutes(trajanjeMin);

		if (terminRepository.brojPreklapanja(profesorId, pocetak, kraj, izuzetiId) > 0) {
			throw new PoslovnaGreskaException(
					"Već imate termin koji se preklapa sa terminom " + pocetak.format(FORMAT) + ".");
		}
	}

	// Otkazan ili već održan termin više ne može da se menja ni otkazuje
	private void proveriDaMozeDaSeMenja(Termin termin) {
		if (termin.getStatus() == StatusTermina.OTKAZAN) {
			throw new PoslovnaGreskaException("Termin je otkazan i ne može da se menja.");
		}
		if (!termin.getPocetak().isAfter(LocalDateTime.now())) {
			throw new PoslovnaGreskaException("Termin je već počeo ili je završen i ne može da se menja.");
		}
	}

	// Kratak opis za poruke, npr. "USP, prof. Zoran Zoranić" ili "opšte, prof. Nikola Nikolić"
	private String opisTermina(Termin termin) {
		String predmet = termin.getPredmet() != null ? termin.getPredmet().getSifra() : "opšte";
		return predmet + ", prof. " + termin.getProfesor().getIme() + " " + termin.getProfesor().getPrezime();
	}

}
