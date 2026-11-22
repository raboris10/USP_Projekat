package rs.ac.singidunum.consultme.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.ac.singidunum.consultme.model.Korisnik;
import rs.ac.singidunum.consultme.model.Notifikacija;
import rs.ac.singidunum.consultme.model.Termin;
import rs.ac.singidunum.consultme.model.TipNotifikacije;
import rs.ac.singidunum.consultme.repository.NotifikacijaRepository;

// JEDNO mesto kroz koje prolaze SVA obaveštenja u aplikaciji.
//
// Za sada: notifikacija se upiše u bazu (korisnik je vidi u aplikaciji) i ispiše u log.
// Kasnije (kontrolna tačka za mejl notifikacije): ovde se doda i slanje mejla,
// a ostatak koda (termini, rezervacije, podsetnici) ostaje isti.
@Service
public class NotifikacijaService {

	private static final Logger log = LoggerFactory.getLogger(NotifikacijaService.class);

	private final NotifikacijaRepository notifikacijaRepository;

	public NotifikacijaService(NotifikacijaRepository notifikacijaRepository) {
		this.notifikacijaRepository = notifikacijaRepository;
	}

	// termin može biti null ako se obaveštenje ne odnosi na konkretan termin
	@Transactional
	public void posalji(Korisnik primalac, TipNotifikacije tip, String poruka, Termin termin) {
		notifikacijaRepository.save(new Notifikacija(primalac, tip, poruka, termin));

		// TODO (mejl notifikacije): ovde poslati mejl na primalac.getEmail()
		log.info("[NOTIFIKACIJA] {} -> {}: {}", tip, primalac.getEmail(), poruka);
	}

}
