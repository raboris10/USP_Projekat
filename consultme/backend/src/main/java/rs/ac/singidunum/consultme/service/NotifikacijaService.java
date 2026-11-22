package rs.ac.singidunum.consultme.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.ac.singidunum.consultme.dto.NotifikacijaDto;
import rs.ac.singidunum.consultme.exception.ResourceNotFoundException;
import rs.ac.singidunum.consultme.exception.ZabranjenPristupException;
import rs.ac.singidunum.consultme.model.Korisnik;
import rs.ac.singidunum.consultme.model.Notifikacija;
import rs.ac.singidunum.consultme.model.Termin;
import rs.ac.singidunum.consultme.model.TipNotifikacije;
import rs.ac.singidunum.consultme.repository.KorisnikRepository;
import rs.ac.singidunum.consultme.repository.NotifikacijaRepository;

// JEDNO mesto kroz koje prolaze SVA obaveštenja u aplikaciji, i njihov prikaz korisniku.
//
// Za sada: notifikacija se upiše u bazu (korisnik je vidi u aplikaciji) i ispiše u log.
// Kasnije (kontrolna tačka za mejl notifikacije): u metodi posalji() se doda i slanje mejla,
// a ostatak koda (termini, rezervacije, podsetnici) ostaje isti.
@Service
public class NotifikacijaService {

	private static final Logger log = LoggerFactory.getLogger(NotifikacijaService.class);

	private final NotifikacijaRepository notifikacijaRepository;
	private final KorisnikRepository korisnikRepository;

	public NotifikacijaService(NotifikacijaRepository notifikacijaRepository, KorisnikRepository korisnikRepository) {
		this.notifikacijaRepository = notifikacijaRepository;
		this.korisnikRepository = korisnikRepository;
	}

	// =====================================================================
	// SLANJE (pozivaju ga drugi servisi)
	// =====================================================================

	// termin može biti null ako se obaveštenje ne odnosi na konkretan termin
	@Transactional
	public void posalji(Korisnik primalac, TipNotifikacije tip, String poruka, Termin termin) {
		notifikacijaRepository.save(new Notifikacija(primalac, tip, poruka, termin));

		// TODO (mejl notifikacije): ovde poslati mejl na primalac.getEmail()
		log.info("[NOTIFIKACIJA] {} -> {}: {}", tip, primalac.getEmail(), poruka);
	}

	// =====================================================================
	// PRIKAZ (za prijavljenog korisnika)
	// =====================================================================

	@Transactional(readOnly = true)
	public List<NotifikacijaDto> moje(String email) {
		Korisnik korisnik = nadjiKorisnika(email);

		return notifikacijaRepository.findByPrimalacIdOrderByKreiranaDesc(korisnik.getId()).stream()
				.map(NotifikacijaDto::od)
				.toList();
	}

	@Transactional(readOnly = true)
	public long brojNeprocitanih(String email) {
		return notifikacijaRepository.countByPrimalacIdAndProcitanaFalse(nadjiKorisnika(email).getId());
	}

	@Transactional
	public NotifikacijaDto oznaciKaoProcitanu(String email, Long id) {
		Notifikacija notifikacija = notifikacijaRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Notifikacija nije pronađena."));

		// Korisnik sme da dira samo svoje notifikacije
		if (!notifikacija.getPrimalac().getEmail().equals(email)) {
			throw new ZabranjenPristupException("Možete menjati samo svoje notifikacije.");
		}

		notifikacija.setProcitana(true);
		return NotifikacijaDto.od(notifikacija);
	}

	// Vraća koliko je notifikacija označeno
	@Transactional
	public int oznaciSveKaoProcitane(String email) {
		return notifikacijaRepository.oznaciSveKaoProcitane(nadjiKorisnika(email).getId());
	}

	private Korisnik nadjiKorisnika(String email) {
		return korisnikRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Korisnik nije pronađen."));
	}

}
