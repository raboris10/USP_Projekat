package rs.ac.singidunum.consultme.dto;

import rs.ac.singidunum.consultme.model.Korisnik;

// Korisnik kako ga vidi administrator - kao KorisnikDto, plus da li je nalog aktivan.
// Lozinka (heš) se ni ovde ne šalje.
public record KorisnikAdminDto(
		Long id,
		String ime,
		String prezime,
		String email,
		String uloga,
		boolean aktivan
) {

	public static KorisnikAdminDto od(Korisnik korisnik) {
		return new KorisnikAdminDto(
				korisnik.getId(),
				korisnik.getIme(),
				korisnik.getPrezime(),
				korisnik.getEmail(),
				korisnik.getUloga().name(),
				korisnik.getAktivan());
	}

}
