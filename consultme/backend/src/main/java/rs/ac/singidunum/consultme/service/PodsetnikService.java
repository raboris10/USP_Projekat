package rs.ac.singidunum.consultme.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.ac.singidunum.consultme.model.Korisnik;
import rs.ac.singidunum.consultme.model.Rezervacija;
import rs.ac.singidunum.consultme.model.StatusRezervacije;
import rs.ac.singidunum.consultme.model.StatusTermina;
import rs.ac.singidunum.consultme.model.Termin;
import rs.ac.singidunum.consultme.model.TipNotifikacije;
import rs.ac.singidunum.consultme.repository.NotifikacijaRepository;
import rs.ac.singidunum.consultme.repository.RezervacijaRepository;
import rs.ac.singidunum.consultme.repository.TerminRepository;

// Automatski podsetnici 24h, 12h i 2h pre termina.
//
// Kako radi: na svakih nekoliko minuta (podsetnici.interval-ms) uzme sve aktivne termine
// koji počinju u narednih 24 sata i za svaki odredi u kom je "prozoru":
//   do početka ostalo  12h - 24h  -> podsetnik 24h
//                       2h - 12h  -> podsetnik 12h
//                       0h -  2h  -> podsetnik  2h
// Podsetnik iz tog prozora šalje se studentima (i profesoru) koji ga još nisu dobili.
//
// Pravila:
// - podsetnik se šalje samo ako je rezervacija napravljena PRE početka prozora
//   (ko rezerviše 5h pre termina ne dobija "24h" ni "12h", nego samo "2h");
// - svaki podsetnik stiže jednom, ali ako je termin pomeren, šalje se ponovo za novo vreme
//   (vidi NotifikacijaRepository.existsBy...KreiranaGreaterThanEqual);
// - profesor dobija podsetnik samo ako je neko prijavljen na termin.
@Service
public class PodsetnikService {

	private static final Logger log = LoggerFactory.getLogger(PodsetnikService.class);

	private final TerminRepository terminRepository;
	private final RezervacijaRepository rezervacijaRepository;
	private final NotifikacijaRepository notifikacijaRepository;
	private final NotifikacijaService notifikacijaService;

	public PodsetnikService(TerminRepository terminRepository, RezervacijaRepository rezervacijaRepository,
			NotifikacijaRepository notifikacijaRepository, NotifikacijaService notifikacijaService) {
		this.terminRepository = terminRepository;
		this.rezervacijaRepository = rezervacijaRepository;
		this.notifikacijaRepository = notifikacijaRepository;
		this.notifikacijaService = notifikacijaService;
	}

	// fixedDelayString -> sledeća provera kreće X milisekundi POSLE završetka prethodne
	// initialDelay -> prva provera 30 sekundi posle pokretanja aplikacije
	@Scheduled(fixedDelayString = "${podsetnici.interval-ms}", initialDelay = 30_000)
	@Transactional
	public void posaljiPodsetnike() {
		LocalDateTime sada = LocalDateTime.now();

		List<Termin> termini = terminRepository.findByStatusAndPocetakAfterAndPocetakLessThanEqual(
				StatusTermina.AKTIVAN, sada, sada.plusHours(24));

		int poslato = 0;
		for (Termin termin : termini) {
			poslato += obradiTermin(termin, sada);
		}

		if (poslato > 0) {
			log.info("[PODSETNICI] Poslato podsetnika: {}", poslato);
		}
	}

	// Vraća broj poslatih podsetnika za ovaj termin
	private int obradiTermin(Termin termin, LocalDateTime sada) {
		// 1) U kom smo prozoru: 2h, 12h ili 24h pre termina
		int sati;
		TipNotifikacije tip;
		if (!termin.getPocetak().isAfter(sada.plusHours(2))) {
			sati = 2;
			tip = TipNotifikacije.PODSETNIK_2H;
		} else if (!termin.getPocetak().isAfter(sada.plusHours(12))) {
			sati = 12;
			tip = TipNotifikacije.PODSETNIK_12H;
		} else {
			sati = 24;
			tip = TipNotifikacije.PODSETNIK_24H;
		}

		// Trenutak kada je prozor počeo, npr. za "24h" -> 24 sata pre početka termina
		LocalDateTime pocetakProzora = termin.getPocetak().minusHours(sati);

		// 2) Samo rezervacije napravljene pre početka prozora
		List<Rezervacija> rezervacije = rezervacijaRepository
				.findByTerminIdAndStatus(termin.getId(), StatusRezervacije.AKTIVNA).stream()
				.filter(r -> r.getKreirana().isBefore(pocetakProzora))
				.toList();

		if (rezervacije.isEmpty()) {
			return 0;
		}

		String vreme = termin.getPocetak().format(TerminService.FORMAT);
		String zaKoliko = (sati == 2) ? "2 sata" : (sati == 12) ? "12 sati" : "24 sata";
		int poslato = 0;

		// 3) Studenti
		for (Rezervacija rezervacija : rezervacije) {
			Korisnik student = rezervacija.getStudent();
			if (nijePoslat(student, termin, tip, pocetakProzora)) {
				notifikacijaService.posalji(student, tip,
						"Podsetnik: termin konsultacija (" + TerminService.opisTermina(termin) + ") počinje za "
								+ zaKoliko + " - " + vreme + ", " + termin.getMesto() + ".",
						termin);
				poslato++;
			}
		}

		// 4) Profesor - jedan podsetnik po terminu
		Korisnik profesor = termin.getProfesor();
		if (nijePoslat(profesor, termin, tip, pocetakProzora)) {
			notifikacijaService.posalji(profesor, tip,
					"Podsetnik: Vaš termin " + vreme + " (" + termin.getMesto() + ") počinje za " + zaKoliko
							+ ". Prijavljenih studenata: " + rezervacije.size() + ".",
					termin);
			poslato++;
		}

		return poslato;
	}

	private boolean nijePoslat(Korisnik primalac, Termin termin, TipNotifikacije tip, LocalDateTime pocetakProzora) {
		return !notifikacijaRepository.existsByPrimalacIdAndTerminIdAndTipAndKreiranaGreaterThanEqual(
				primalac.getId(), termin.getId(), tip, pocetakProzora);
	}

}
