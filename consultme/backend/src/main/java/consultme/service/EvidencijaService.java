package consultme.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import consultme.exception.ConsultMeException;
import consultme.model.Evidencija;
import consultme.model.Rezervacija;
import consultme.model.StatusRezervacije;
import consultme.model.Termin;
import consultme.repository.EvidencijaRepository;
import consultme.repository.RezervacijaRepository;
import consultme.repository.TerminRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class EvidencijaService {

	@Autowired
	private EvidencijaRepository evidencijaRepository;
	@Autowired
	private RezervacijaRepository rezervacijaRepository;
	@Autowired
	private TerminRepository terminRepository;

	// nastavnik belezi da li je student dosao (moze se naknadno promeniti)
	@Transactional
	public Rezervacija oznaci(String emailNastavnika, Long rezervacijaId, boolean dosao) {
		Rezervacija rezervacija = rezervacijaRepository.findById(rezervacijaId)
				.orElseThrow(() -> new EntityNotFoundException("Rezervacija sa ID " + rezervacijaId + " nije pronadjena"));
		if (!rezervacija.getTermin().getNastavnik().getEmail().equals(emailNastavnika)) {
			throw new AccessDeniedException("Mozete evidentirati samo dolaske na svoje termine");
		}
		if (rezervacija.getStatus() != StatusRezervacije.AKTIVNA) {
			throw new ConsultMeException("Otkazana rezervacija se ne evidentira");
		}

		Evidencija evidencija = rezervacija.getEvidencija();
		if (evidencija == null) {
			evidencija = new Evidencija();
			evidencija.setRezervacija(rezervacija);
			rezervacija.setEvidencija(evidencija);
		}
		evidencija.setDosao(dosao);
		evidencijaRepository.save(evidencija);
		return rezervacija;
	}

	// izvoz liste studenata jednog termina u CSV formatu
	public String izvozCsv(String emailNastavnika, Long terminId) {
		Termin termin = terminRepository.findById(terminId)
				.orElseThrow(() -> new EntityNotFoundException("Termin nije pronadjen"));
		if (!termin.getNastavnik().getEmail().equals(emailNastavnika)) {
			throw new AccessDeniedException("Mozete izvoziti samo svoje termine");
		}

		StringBuilder sb = new StringBuilder("brojIndeksa,ime,prezime,email,tema,dosao\n");
		for (Rezervacija r : rezervacijaRepository.findByTerminIdOrderByKreiranaAsc(terminId)) {
			if (r.getStatus() != StatusRezervacije.AKTIVNA) {
				continue;
			}
			String dosao = r.getEvidencija() == null ? "" : (r.getEvidencija().isDosao() ? "da" : "ne");
			sb.append(csv(r.getStudent().getBrojIndeksa())).append(',')
			  .append(csv(r.getStudent().getIme())).append(',')
			  .append(csv(r.getStudent().getPrezime())).append(',')
			  .append(csv(r.getStudent().getEmail())).append(',')
			  .append(csv(r.getTema())).append(',')
			  .append(dosao).append('\n');
		}
		return sb.toString();
	}

	private String csv(String vrednost) {
		if (vrednost == null) {
			return "";
		}
		return "\"" + vrednost.replace("\"", "\"\"") + "\"";
	}
}
