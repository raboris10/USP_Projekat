package rs.ac.singidunum.consultme.dto;

import java.util.Comparator;
import java.util.List;

import rs.ac.singidunum.consultme.model.Korisnik;
import rs.ac.singidunum.consultme.model.Predmet;

// Podaci o predmetu koje backend vraća frontendu.
// Uz predmet šaljemo i naziv katedre i listu profesora koji ga drže,
// da frontend ne bi morao da pravi dodatne zahteve za to.
public record PredmetDto(
		Long id,
		String naziv,
		String sifra,
		Long katedraId,
		String katedraNaziv,
		List<KorisnikDto> profesori
) {

	// Poziva se unutar transakcije (servis), jer su katedra i profesori LAZY
	public static PredmetDto od(Predmet predmet) {
		List<KorisnikDto> profesori = predmet.getProfesori().stream()
				.sorted(Comparator.comparing(Korisnik::getPrezime))   // abecedno po prezimenu
				.map(KorisnikDto::od)
				.toList();

		return new PredmetDto(
				predmet.getId(),
				predmet.getNaziv(),
				predmet.getSifra(),
				predmet.getKatedra().getId(),
				predmet.getKatedra().getNaziv(),
				profesori);
	}

}
