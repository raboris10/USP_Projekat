package rs.ac.singidunum.consultme.dto;

import java.time.LocalDateTime;

import rs.ac.singidunum.consultme.model.Notifikacija;

// Notifikacija kako je vidi korisnik.
// terminId -> frontend može da napravi link ka terminu (null ako se ne odnosi na termin).
public record NotifikacijaDto(
		Long id,
		String tip,
		String poruka,
		Long terminId,
		boolean procitana,
		LocalDateTime kreirana
) {

	public static NotifikacijaDto od(Notifikacija notifikacija) {
		return new NotifikacijaDto(
				notifikacija.getId(),
				notifikacija.getTip().name(),
				notifikacija.getPoruka(),
				notifikacija.getTermin() != null ? notifikacija.getTermin().getId() : null,
				notifikacija.getProcitana(),
				notifikacija.getKreirana());
	}

}
