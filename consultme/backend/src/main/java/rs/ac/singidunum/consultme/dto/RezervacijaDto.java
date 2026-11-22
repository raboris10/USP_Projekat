package rs.ac.singidunum.consultme.dto;

import java.time.LocalDateTime;

import rs.ac.singidunum.consultme.model.Rezervacija;

// Rezervacija kako je vidi student: njegova tema i status, plus svi podaci o terminu.
// mozeDaSeOtkaze -> backend već izračuna, da frontend zna da li da prikaže dugme "Otkaži".
public record RezervacijaDto(
		Long id,
		String tema,
		String status,          // AKTIVNA ili OTKAZANA
		String dolazak,         // null dok profesor ne upiše, pa DOSAO ili NIJE_DOSAO
		LocalDateTime kreirana,
		boolean mozeDaSeOtkaze,
		TerminDto termin
) {

	public static RezervacijaDto od(Rezervacija rezervacija, boolean mozeDaSeOtkaze, TerminDto termin) {
		return new RezervacijaDto(
				rezervacija.getId(),
				rezervacija.getTema(),
				rezervacija.getStatus().name(),
				rezervacija.getDolazak() != null ? rezervacija.getDolazak().name() : null,
				rezervacija.getKreirana(),
				mozeDaSeOtkaze,
				termin);
	}

}
