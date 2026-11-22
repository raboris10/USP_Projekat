package rs.ac.singidunum.consultme.dto;

import java.time.LocalDateTime;

import rs.ac.singidunum.consultme.model.Rezervacija;

// Jedna rezervacija na terminu, kako je vidi profesor:
// ko je prijavljen, šta želi da pita i (kasnije) da li je došao.
public record PrijavljeniStudentDto(
		Long rezervacijaId,
		Long studentId,
		String ime,
		String prezime,
		String email,
		String tema,
		String status,         // AKTIVNA ili OTKAZANA
		String dolazak,        // null dok profesor ne upiše, pa DOSAO ili NIJE_DOSAO
		LocalDateTime kreirana
) {

	public static PrijavljeniStudentDto od(Rezervacija rezervacija) {
		return new PrijavljeniStudentDto(
				rezervacija.getId(),
				rezervacija.getStudent().getId(),
				rezervacija.getStudent().getIme(),
				rezervacija.getStudent().getPrezime(),
				rezervacija.getStudent().getEmail(),
				rezervacija.getTema(),
				rezervacija.getStatus().name(),
				rezervacija.getDolazak() != null ? rezervacija.getDolazak().name() : null,
				rezervacija.getKreirana());
	}

}
