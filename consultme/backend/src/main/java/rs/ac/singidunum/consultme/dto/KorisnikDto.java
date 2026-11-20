package rs.ac.singidunum.consultme.dto;

import rs.ac.singidunum.consultme.model.Korisnik;

// Podaci o korisniku koje smemo da pošaljemo frontendu (BEZ lozinke!)
public record KorisnikDto(
		Long id,
		String ime,
		String prezime,
		String email,
		String uloga
) {

	// Pomoćna metoda: pretvara entitet Korisnik u DTO
	public static KorisnikDto od(Korisnik korisnik) {
		return new KorisnikDto(
				korisnik.getId(),
				korisnik.getIme(),
				korisnik.getPrezime(),
				korisnik.getEmail(),
				korisnik.getUloga().name());   // enum -> tekst, npr. "STUDENT"
	}

}
