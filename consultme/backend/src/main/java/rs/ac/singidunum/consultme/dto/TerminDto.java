package rs.ac.singidunum.consultme.dto;

import java.time.LocalDateTime;

import rs.ac.singidunum.consultme.model.Predmet;
import rs.ac.singidunum.consultme.model.Termin;

// Podaci o terminu koje backend vraća frontendu.
// kraj i slobodnihMesta se ne čuvaju u bazi - računaju se ovde, da frontend ne mora.
public record TerminDto(
		Long id,
		LocalDateTime pocetak,
		LocalDateTime kraj,
		Integer trajanjeMin,
		String mesto,
		Integer kapacitet,
		long brojPrijavljenih,
		long slobodnihMesta,
		String status,
		Long profesorId,
		String profesorIme,
		String profesorPrezime,
		Long predmetId,        // null ako su konsultacije opšte (bez predmeta)
		String predmetNaziv,
		String predmetSifra
) {

	// brojPrijavljenih = broj AKTIVNIH rezervacija; računa ga servis
	// Poziva se unutar transakcije (servis), jer su profesor i predmet LAZY
	public static TerminDto od(Termin termin, long brojPrijavljenih) {
		Predmet predmet = termin.getPredmet();

		return new TerminDto(
				termin.getId(),
				termin.getPocetak(),
				termin.getPocetak().plusMinutes(termin.getTrajanjeMin()),
				termin.getTrajanjeMin(),
				termin.getMesto(),
				termin.getKapacitet(),
				brojPrijavljenih,
				Math.max(0, termin.getKapacitet() - brojPrijavljenih),
				termin.getStatus().name(),
				termin.getProfesor().getId(),
				termin.getProfesor().getIme(),
				termin.getProfesor().getPrezime(),
				predmet != null ? predmet.getId() : null,
				predmet != null ? predmet.getNaziv() : null,
				predmet != null ? predmet.getSifra() : null);
	}

}
