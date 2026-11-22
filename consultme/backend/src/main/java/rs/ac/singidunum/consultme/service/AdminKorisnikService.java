package rs.ac.singidunum.consultme.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.ac.singidunum.consultme.dto.KorisnikAdminDto;
import rs.ac.singidunum.consultme.dto.KorisnikIzmenaRequest;
import rs.ac.singidunum.consultme.dto.KorisnikRequest;
import rs.ac.singidunum.consultme.exception.PoslovnaGreskaException;
import rs.ac.singidunum.consultme.exception.ResourceNotFoundException;
import rs.ac.singidunum.consultme.model.Korisnik;
import rs.ac.singidunum.consultme.model.Rezervacija;
import rs.ac.singidunum.consultme.model.StatusRezervacije;
import rs.ac.singidunum.consultme.model.StatusTermina;
import rs.ac.singidunum.consultme.model.Termin;
import rs.ac.singidunum.consultme.model.TipNotifikacije;
import rs.ac.singidunum.consultme.model.Uloga;
import rs.ac.singidunum.consultme.repository.KorisnikRepository;
import rs.ac.singidunum.consultme.repository.PredmetRepository;
import rs.ac.singidunum.consultme.repository.RezervacijaRepository;
import rs.ac.singidunum.consultme.repository.TerminRepository;

// Administrator upravlja korisnicima: dodaje ih, menja, resetuje lozinku, deaktivira i aktivira.
// Korisnici se NE brišu - deaktivacija čuva istoriju termina i rezervacija.
@Service
public class AdminKorisnikService {

	private final KorisnikRepository korisnikRepository;
	private final TerminRepository terminRepository;
	private final RezervacijaRepository rezervacijaRepository;
	private final PredmetRepository predmetRepository;
	private final PasswordEncoder passwordEncoder;
	private final NotifikacijaService notifikacijaService;

	public AdminKorisnikService(KorisnikRepository korisnikRepository, TerminRepository terminRepository,
			RezervacijaRepository rezervacijaRepository, PredmetRepository predmetRepository,
			PasswordEncoder passwordEncoder, NotifikacijaService notifikacijaService) {
		this.korisnikRepository = korisnikRepository;
		this.terminRepository = terminRepository;
		this.rezervacijaRepository = rezervacijaRepository;
		this.predmetRepository = predmetRepository;
		this.passwordEncoder = passwordEncoder;
		this.notifikacijaService = notifikacijaService;
	}

	// =====================================================================
	// PREGLED
	// =====================================================================

	// Lista korisnika; oba filtera su opciona
	@Transactional(readOnly = true)
	public List<KorisnikAdminDto> pretrazi(Uloga uloga, String pretraga) {
		// "Ana" -> "%ana%" (LIKE traži tekst bilo gde u imenu/email-u, bez obzira na velika slova)
		String uzorak = (pretraga == null || pretraga.isBlank())
				? null
				: "%" + pretraga.trim().toLowerCase() + "%";

		return korisnikRepository.pretrazi(uloga, uzorak).stream()
				.map(KorisnikAdminDto::od)
				.toList();
	}

	@Transactional(readOnly = true)
	public KorisnikAdminDto findById(Long id) {
		return KorisnikAdminDto.od(nadji(id));
	}

	// =====================================================================
	// DODAVANJE, IZMENA, RESET LOZINKE
	// =====================================================================

	@Transactional
	public KorisnikAdminDto create(KorisnikRequest zahtev) {
		String email = normalizujEmail(zahtev.email());

		if (korisnikRepository.existsByEmailIgnoreCase(email)) {
			throw new PoslovnaGreskaException("Korisnik sa email-om " + email + " već postoji.");
		}

		// Lozinka se u bazu upisuje samo kao BCrypt heš
		Korisnik korisnik = new Korisnik(
				zahtev.ime().trim(),
				zahtev.prezime().trim(),
				email,
				passwordEncoder.encode(zahtev.lozinka()),
				zahtev.uloga());

		return KorisnikAdminDto.od(korisnikRepository.save(korisnik));
	}

	@Transactional
	public KorisnikAdminDto update(String emailAdmina, Long id, KorisnikIzmenaRequest zahtev) {
		Korisnik korisnik = nadji(id);
		String email = normalizujEmail(zahtev.email());

		if (korisnikRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
			throw new PoslovnaGreskaException("Korisnik sa email-om " + email + " već postoji.");
		}

		if (korisnik.getUloga() != zahtev.uloga()) {
			proveriPromenuUloge(emailAdmina, korisnik);
		}

		korisnik.setIme(zahtev.ime().trim());
		korisnik.setPrezime(zahtev.prezime().trim());
		korisnik.setEmail(email);
		korisnik.setUloga(zahtev.uloga());

		return KorisnikAdminDto.od(korisnik);
	}

	@Transactional
	public void resetujLozinku(Long id, String novaLozinka) {
		Korisnik korisnik = nadji(id);
		korisnik.setLozinka(passwordEncoder.encode(novaLozinka));
	}

	// =====================================================================
	// DEAKTIVACIJA I AKTIVACIJA
	// =====================================================================

	@Transactional
	public KorisnikAdminDto deaktiviraj(String emailAdmina, Long id) {
		Korisnik korisnik = nadji(id);
		LocalDateTime sada = LocalDateTime.now();

		if (korisnik.getEmail().equals(emailAdmina)) {
			throw new PoslovnaGreskaException("Ne možete deaktivirati sopstveni nalog.");
		}
		if (!korisnik.getAktivan()) {
			throw new PoslovnaGreskaException("Nalog je već deaktiviran.");
		}

		// Profesor: prvo mora sam da otkaže buduće termine (studenti tada dobiju obaveštenje)
		if (korisnik.getUloga() == Uloga.PROFESOR
				&& terminRepository.existsByProfesorIdAndStatusAndPocetakAfter(korisnik.getId(), StatusTermina.AKTIVAN, sada)) {
			throw new PoslovnaGreskaException(
					"Profesor ima zakazane buduće termine. Termini moraju da se otkažu pre deaktivacije naloga.");
		}

		// Student: njegove buduće rezervacije se otkazuju, a profesori dobijaju obaveštenje
		if (korisnik.getUloga() == Uloga.STUDENT) {
			List<Rezervacija> buduce = rezervacijaRepository.findByStudentIdAndStatusAndTerminPocetakAfter(
					korisnik.getId(), StatusRezervacije.AKTIVNA, sada);

			for (Rezervacija rezervacija : buduce) {
				rezervacija.setStatus(StatusRezervacije.OTKAZANA);

				Termin termin = rezervacija.getTermin();
				notifikacijaService.posalji(termin.getProfesor(), TipNotifikacije.REZERVACIJA_OTKAZANA,
						"Otkazana rezervacija za Vaš termin " + termin.getPocetak().format(TerminService.FORMAT)
								+ ": " + korisnik.getIme() + " " + korisnik.getPrezime() + " (nalog je deaktiviran).",
						termin);
			}
		}

		korisnik.setAktivan(false);
		return KorisnikAdminDto.od(korisnik);
	}

	@Transactional
	public KorisnikAdminDto aktiviraj(Long id) {
		Korisnik korisnik = nadji(id);

		if (korisnik.getAktivan()) {
			throw new PoslovnaGreskaException("Nalog je već aktivan.");
		}

		korisnik.setAktivan(true);
		return KorisnikAdminDto.od(korisnik);
	}

	// =====================================================================
	// POMOĆNE METODE
	// =====================================================================

	private Korisnik nadji(Long id) {
		return korisnikRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Korisnik nije pronađen."));
	}

	private String normalizujEmail(String email) {
		return email.trim().toLowerCase();   // isto kao pri prijavi (AuthService)
	}

	// Uloga sme da se promeni samo korisniku koji još nema istoriju u toj ulozi,
	// inače bi npr. "student" ostao vlasnik termina ili "profesor" vlasnik rezervacija.
	private void proveriPromenuUloge(String emailAdmina, Korisnik korisnik) {
		if (korisnik.getEmail().equals(emailAdmina)) {
			throw new PoslovnaGreskaException("Ne možete promeniti sopstvenu ulogu.");
		}

		if (korisnik.getUloga() == Uloga.PROFESOR) {
			if (terminRepository.existsByProfesorId(korisnik.getId())) {
				throw new PoslovnaGreskaException("Profesoru koji ima termine ne može da se promeni uloga.");
			}
			if (predmetRepository.existsByProfesoriId(korisnik.getId())) {
				throw new PoslovnaGreskaException(
						"Profesor je dodeljen predmetima. Prvo ga uklonite sa predmeta, pa promenite ulogu.");
			}
		}

		if (korisnik.getUloga() == Uloga.STUDENT && rezervacijaRepository.existsByStudentId(korisnik.getId())) {
			throw new PoslovnaGreskaException("Studentu koji ima rezervacije ne može da se promeni uloga.");
		}
	}

}
