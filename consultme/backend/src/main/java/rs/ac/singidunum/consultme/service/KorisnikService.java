package rs.ac.singidunum.consultme.service;

import java.util.List;

import org.springframework.stereotype.Service;

import rs.ac.singidunum.consultme.dto.KorisnikDto;
import rs.ac.singidunum.consultme.exception.ResourceNotFoundException;
import rs.ac.singidunum.consultme.model.Uloga;
import rs.ac.singidunum.consultme.repository.KorisnikRepository;

// Poslovna logika vezana za korisnike
@Service
public class KorisnikService {

	private final KorisnikRepository korisnikRepository;

	public KorisnikService(KorisnikRepository korisnikRepository) {
		this.korisnikRepository = korisnikRepository;
	}

	// Vraća podatke o korisniku sa datim email-om (bez lozinke - zato DTO)
	public KorisnikDto findByEmail(String email) {
		return korisnikRepository.findByEmail(email)
				.map(KorisnikDto::od)   // ako postoji: pretvori Korisnik -> KorisnikDto
				.orElseThrow(() -> new ResourceNotFoundException("Korisnik nije pronađen."));
	}

	// Svi aktivni profesori, abecedno (za filter pri pretrazi termina)
	public List<KorisnikDto> findProfesori() {
		return korisnikRepository.findByUlogaAndAktivanTrueOrderByPrezimeAscImeAsc(Uloga.PROFESOR).stream()
				.map(KorisnikDto::od)
				.toList();
	}

}
